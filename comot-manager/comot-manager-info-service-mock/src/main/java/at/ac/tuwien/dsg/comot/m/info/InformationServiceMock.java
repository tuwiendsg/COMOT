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
package at.ac.tuwien.dsg.comot.m.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Modifier;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@Component
public class InformationServiceMock {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Map<String, Template> templates = Collections.synchronizedMap(new HashMap<String, Template>());
	protected Map<String, CloudService> services = Collections.synchronizedMap(new HashMap<String, CloudService>());
	protected Map<String, OfferedServiceUnit> osus = Collections
			.synchronizedMap(new HashMap<String, OfferedServiceUnit>());
	protected Map<String, OsuInstance> osuInstances = Collections
			.synchronizedMap(new HashMap<String, OsuInstance>());

	protected int serviceItarator = 0;
	protected int osuInstanceItarator = 0;

	public void deleteAll() {
		templates = Collections.synchronizedMap(new HashMap<String, Template>());
		services = Collections.synchronizedMap(new HashMap<String, CloudService>());
		osus = Collections.synchronizedMap(new HashMap<String, OfferedServiceUnit>());
		osuInstances = Collections.synchronizedMap(new HashMap<String, OsuInstance>());
		serviceItarator = 0;
		osuInstanceItarator = 0;
	}

	protected synchronized String getInstanceId(String serviceId) {
		return serviceId + "_" + ++serviceItarator;
	}

	protected synchronized String getOsuInstanceId(String osuId) {
		return osuId + "_" + ++osuInstanceItarator;
	}

	// TEMPLATE

	public void reserveServiceId(String serviceId) {

	}

	public void createTemplate(String templateId, CloudService service) {

		if (templates.containsKey(templateId)) {
			throw new ComotIllegalArgumentException("A Template with the ID '" + templateId + "' alreadty exists.");
		}

		service.setDateCreated(System.currentTimeMillis());

		templates.put(templateId, new Template(templateId, service));
	}

	public void removeTemplate(String templateId) {

		if (!templates.containsKey(templateId)) {
			throw new ComotIllegalArgumentException("There is no service '" + templateId + "'");
		}

		templates.remove(templateId);
	}

	public List<Template> getTemplates() {
		return new ArrayList(templates.values());
	}

	// SERVICE

	public String createService(CloudService service) throws ClassNotFoundException, IOException {

		if (services.containsKey(service.getId())) {
			throw new ComotIllegalArgumentException("A Template with the ID '" + service.getId() + "' alreadty exists.");
		}

		service.setDateCreated(System.currentTimeMillis());
		services.put(service.getId(), service);

		return service.getId();
	}

	public String createServiceFromTemplate(String templateId) throws ClassNotFoundException, IOException {

		log.info("createServiceFromTemplate {}", templateId);

		CloudService templateServ = templates.get(templateId).getDescription();
		String serviceId = getInstanceId(templateId);
		CloudService service = (CloudService) Utils.deepCopy(templateServ);
		service.setId(serviceId);
		service.setName(serviceId);
		service.setDateCreated(System.currentTimeMillis());

		services.put(serviceId, service);

		return serviceId;
	}

	public List<CloudService> getServices() {
		return new ArrayList<CloudService>(services.values());
	}

	public CloudService getService(String serviceId) {
		return services.get(serviceId);
	}

	public void removeService(String serviceId) {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("There is no service '" + serviceId + "'");
		}

		services.remove(serviceId);
	}

	public void reconfigureElasticity(String serviceId, CloudService elConfig) {

		CloudService service = services.get(serviceId);
		Modifier.replaceSyblDirectives(elConfig, service);

	}

	// UNIT INSTANCE

	public void putUnitInstance(String serviceId, String unitId, UnitInstance uInst) {

		removeUnitInstance(serviceId, uInst.getId());
		addUnitInstance(serviceId, unitId, uInst);
	}

	public void addUnitInstance(String serviceId, String unitId, UnitInstance uInst) {

		Navigator nav = new Navigator(services.get(serviceId));
		nav.getUnit(unitId).addUnitInstance(uInst);
	}

	public void removeUnitInstance(String serviceId, String uInstId) {

		Navigator nav = new Navigator(services.get(serviceId));
		UnitInstance uInst = nav.getInstance(uInstId);

		if (uInst != null) {
			nav.getUnitFor(uInstId).getInstances().remove(uInst);
		}
	}

	// OSU

	public void addOsu(OfferedServiceUnit osu) {

		if (osu.getServiceTemplate() != null) {
			templates.put(osu.getServiceTemplate().getId(), osu.getServiceTemplate());
		}

		osus.put(osu.getId(), osu);
	}

	//
	// public String createOsuInstance(String osuId) {
	//
	// OfferedServiceUnit osu = osus.get(osuId);
	// String osuInstanceId = getOsuInstanceId(osuId);
	// OsuInstance osuInstance = new OsuInstance(osuInstanceId, osu);
	// osuInstances.put(osuInstanceId, osuInstance);
	// log.info("createOsuInstance(osuId={}):{}", osuId, osuInstanceId);
	// return osuInstanceId;
	// }

	public String createOsuInstance(String osuId) throws ClassNotFoundException, IOException {

		OfferedServiceUnit osu = osus.get(osuId);
		String osuInstanceId = getOsuInstanceId(osuId);

		// create osuInstance
		OsuInstance osuInstance = new OsuInstance(osuInstanceId, osu);
		osuInstances.put(osuInstanceId, osuInstance);
		log.info("createOsuInstance(osuId={}):{}", osuId, osuInstanceId);

		if (osu.getServiceTemplate() != null) {
			// create service
			String templateId = osu.getServiceTemplate().getId();
			log.info("templateId {}", templateId);
			String serviceId = createServiceFromTemplate(templateId);

			osuInstances.get(osuInstanceId).setService(services.get(serviceId));
		}

		return osuInstanceId;
	}

	public void removeOsuInatance(String osuInstanceId) {
		osuInstances.remove(osuInstanceId);
	}

	public void assignSupportingService(String serviceId, String osuInstanceId) {

		if (isOsuAssignedToInstance(serviceId, osuInstanceId)) {
			return;
		}

		services.get(serviceId).getSupport().add(osuInstances.get(osuInstanceId));
	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String osuInstanceId) {

		if (isOsuAssignedToInstance(serviceId, osuInstanceId)) {

			CloudService service = services.get(serviceId);

			for (OsuInstance osuInstance : service.getSupport()) {
				if (osuInstance.getId().equals(osuInstanceId)) {
					service.getSupport().remove(osuInstance);
					return;
				}
			}
		}
	}

	private boolean isOsuAssignedToInstance(String serviceId, String osuInstanceId) {

		boolean value = false;

		log.info("isOsuAssignedToInstance {} {}", serviceId, osuInstanceId);

		for (OsuInstance osuInstance : services.get(serviceId).getSupport()) {
			if (osuInstance.getId().equals(osuInstanceId)) {
				value = true;
			}
		}

		log.info("isOsuAssignedToInstance( instanceId={}, osuInstanceId={}): {}", serviceId, osuInstanceId, value);
		return value;
	}

	public List<OfferedServiceUnit> getOsus() {
		return new ArrayList<OfferedServiceUnit>(osus.values());
	}

	public List<OsuInstance> getOsusInstances() {
		return new ArrayList<OsuInstance>(osuInstances.values());
	}

}
