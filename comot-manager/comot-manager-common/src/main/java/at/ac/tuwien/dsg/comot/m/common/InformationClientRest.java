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
package at.ac.tuwien.dsg.comot.m.common;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public class InformationClientRest extends ServiceClient {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceClient.class);

	public InformationClientRest(URI baseUri) {
		super("INFO_SERVICE", baseUri);
	}

	public InformationClientRest() {
		super();
	}

	// TEMPLATE

	public String createTemplate(CloudService service) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.TEMPLATES)
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.xml(service));

		processResponseStatus(response);
		String result = response.readEntity(String.class);

		return result;
	}

	public void removeTemplate(String templateId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.TEMPLATES_ONE)
				.resolveTemplate("templateId", templateId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	public List<Template> getTemplates() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.TEMPLATES)
				.request(MediaType.WILDCARD)
				.get();

		processResponseStatus(response);

		final GenericType<List<Template>> list = new GenericType<List<Template>>() {
		};

		List<Template> result = response.readEntity(list);

		return result;
	}

	// SERVICE

	public String createService(CloudService service) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.SERVICES)
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.xml(service));
		processResponseStatus(response);
		String result = response.readEntity(String.class);

		return result;
	}

	public String createServiceFromTemplate(String templateId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.TEMPLATES_ONE_SERVICES)
				.resolveTemplate("templateId", templateId)
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.text(""));
		processResponseStatus(response);
		String result = response.readEntity(String.class);

		return result;
	}

	public void removeService(String serviceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.SERVICE_ONE)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	public List<CloudService> getServices() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.SERVICES)
				.request(MediaType.APPLICATION_XML)
				.get();
		processResponseStatus(response);
		final GenericType<List<CloudService>> list = new GenericType<List<CloudService>>() {
		};

		List<CloudService> result = response.readEntity(list);

		return result;
	}

	public void reconfigureElasticity(String serviceId, CloudService service) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.SERVICE_ONE_ELASTICITY)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_PLAIN)
				.put(Entity.xml(service));
		processResponseStatus(response);
	}

	// public CloudService getService(String serviceId) {
	//
	// Response response = client.target(baseUri)
	// .path(SERVICE_ONE)
	// .resolveTemplate("serviceId", serviceId)
	// .request(MediaType.APPLICATION_XML)
	// .get();
	//
	// CloudService result = response.readEntity(CloudService.class);
	//
	// return result;
	// }

	// UNIT INSTANCES

	public void putUnitInstance(String serviceId, String unitId, UnitInstance uInst)
			throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.UNIT_INSTANCE_ONE)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("unitId", unitId)
				.resolveTemplate("unitInstanceId", uInst.getId())
				.request(MediaType.WILDCARD)
				.put(Entity.xml(uInst));

		processResponseStatus(response);
		// CloudService result = response.readEntity(CloudService.class);

	}

	public void removeUnitInstance(String serviceId, String uInstId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.UNIT_INSTANCE_ONE)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("unitId", "ANY")
				.resolveTemplate("unitInstanceId", uInstId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	// OSU

	public void addOsu(OfferedServiceUnit osu) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPSES)
				.request(MediaType.WILDCARD)
				.post(Entity.xml(osu));

		processResponseStatus(response);
	}

	public List<OfferedServiceUnit> getOsus() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPSES)
				.request(MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);
		final GenericType<List<OfferedServiceUnit>> list = new GenericType<List<OfferedServiceUnit>>() {
		};

		List<OfferedServiceUnit> result = response.readEntity(list);

		return result;
	}

	public List<OsuInstance> getOsuInstances() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPS_INSTANCES_ALL)
				.request(MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);
		final GenericType<List<OsuInstance>> list = new GenericType<List<OsuInstance>>() {
		};

		List<OsuInstance> result = response.readEntity(list);

		return result;
	}

	public String createOsuInstance(String osuId, String optionalServiceInstanceId, String osuInstanceId)
			throws EpsException {

		String body = (optionalServiceInstanceId == null) ? "" : optionalServiceInstanceId + " " + osuInstanceId;

		Response response = client.target(baseUri)
				.path(Constants.EPS_ONE_INSTANCES)
				.resolveTemplate("epsId", osuId)
				.request(MediaType.WILDCARD)
				.post(Entity.text(body));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		return result;
	}

	public void removeOsuInatance(String osuInstanceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPS_INSTANCE_ONE)
				.resolveTemplate("epsInstanceId", osuInstanceId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	public void assignEps(String serviceId, String osuInstanceId) throws EpsException {

		LOG.info("assignEps({} {})", serviceId, osuInstanceId);

		Response response = client.target(baseUri)
				.path(Constants.EPS_INSTANCE_ASSIGNMENT)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("epsId", osuInstanceId)
				.request(MediaType.WILDCARD)
				.put(Entity.text(""));

		processResponseStatus(response);
	}

	public void removeEpsAssignment(String instanceId, String osuInstanceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPS_INSTANCE_ASSIGNMENT)
				.resolveTemplate("serviceId", instanceId)
				.resolveTemplate("epsId", osuInstanceId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	public void deleteAll() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.DELETE_ALL)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

}
