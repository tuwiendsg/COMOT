/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.cs;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class DeploymentClientSalsa implements DeploymentClient {

	private final Logger log = LoggerFactory.getLogger(DeploymentClientSalsa.class);

	protected SalsaClient salsa;
	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;

	public DeploymentClientSalsa(SalsaClient salsa) {
		super();
		this.salsa = salsa;
	}

	@Override
	public CloudService deploy(CloudService service) throws EpsException, ComotException {

		String toscaDescriptionXml;

		if (service == null) {
			log.warn("deploy(service=null )");
			return null;
		}

		try {
			toscaDescriptionXml = UtilsCs.asString(mapperTosca.extractTosca(service));
		} catch (JAXBException e) {
			throw new ComotException("Failed to marshall TOSCA into XML ", e);
		}

		CloudService deployedService = deploy(toscaDescriptionXml);

		return deployedService;
	}

	@Override
	public CloudService deploy(String service) throws EpsException, ComotException {

		String serviceId = salsa.deploy(service);

		Definitions def = salsa.getTosca(serviceId);
		CloudService deployedService = mapperTosca.createModel(def);

		return deployedService;
	}

	@Override
	public void undeploy(String serviceId) throws EpsException {
		salsa.undeploy(serviceId);
	}

	@Override
	public void spawn(String serviceId, String topologyId, String nodeId, int instanceCount)
			throws EpsException {
		salsa.spawn(serviceId, topologyId, nodeId, instanceCount);
	}

	@Override
	public void destroy(String serviceId, String topologyId, String nodeId, int instanceId) throws EpsException {
		salsa.destroy(serviceId, topologyId, nodeId, instanceId);
	}

	@Override
	public CloudService getService(String serviceId) throws EpsException, ComotException {

		Definitions definitions = salsa.getTosca(serviceId);

		return mapperTosca.createModel(definitions);
	}

	@Override
	public CloudService refreshStatus(CloudService service)
			throws EpsException, ComotException {

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService status = salsa.getStatus(service.getId());

		CloudService copy;
		try {
			copy = (CloudService) Utils.deepCopy(service);
		} catch (ClassNotFoundException | IOException e) {
			throw new ComotException("Failed to deep-copy CloudService id=" + service.getId(), e);
		}

		mapperDepl.enrichModel(service.getId(), copy, status);

		return copy;
	}

	@Override
	public CloudService refreshStatus(Map<String, String> map, CloudService service) throws EpsException,
			ComotException {

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService status = salsa.getStatus(service.getId());

		CloudService copy;
		try {
			copy = (CloudService) Utils.deepCopy(service);
		} catch (ClassNotFoundException | IOException e) {
			throw new ComotException("Failed to deep-copy CloudService id=" + service.getId(), e);
		}

		mapperDepl.enrichModel(service.getId(), copy, status);

		map.putAll(mapperDepl.extractStates(service.getId(), status));

		return copy;
	}

	@Override
	public boolean isRunning(String serviceId) throws EpsException, ComotException {

		boolean running = false;

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service = salsa.getStatus(serviceId);

		log.info("Service {} is in state={}", serviceId, service.getState());

		if (service.getState().equals(SalsaEntityState.DEPLOYED)) {
			running = true;

			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit serviceUnit : service
					.getAllComponentByType(SalsaEntityType.SOFTWARE)) {

				if (serviceUnit != null
						&& !(serviceUnit.getState().equals(SalsaEntityState.DEPLOYED))) {
					running = false;
					break;
				}
			}
		}

		return running;
	}

	@Override
	public boolean isManaged(String serviceId) throws EpsException {

		if (serviceId == null) {
			throw new IllegalArgumentException("serviceId can not be null");
		}

		String msg = salsa.getServices();

		JSONObject services = new JSONObject(msg);
		JSONArray array = services.getJSONArray("services");

		for (int i = 0; i < array.length(); i++) {
			if (array.getJSONObject(i).getString("serviceId").equals(serviceId)) {
				return true;
			}
		}

		return false;
	}

	@PreDestroy
	public void cleanup() {
		if (salsa != null) {
			log.info("closing salsa client");
			salsa.close();
		}
	}

	@Override
	public void setHostAndPort(String host, int port) {
		salsa.setBaseUri(UriBuilder.fromUri(salsa.getBaseUri()).host(host).port(port).build());
	}

}
