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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

public class InformationClient {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String NON_EPS = "NON_EPS";
	public static final String EPS = "EPS";
	public static final String ALL = "ALL";
	public static final String STATIC = "STATIC";
	public static final String DYNAMIC = "DYNAMIC";

	protected InformationClientRest client;

	public InformationClient() {

	}

	public InformationClient(InformationClientRest client) {
		super();
		this.client = client;
	}

	// TEMPLATE
	public String createTemplate(CloudService service) throws EpsException {
		return client.createTemplate(service);
	}

	public void removeTemplate(String templateId) throws EpsException {
		client.removeTemplate(templateId);
	}

	public List<Template> getTemplates() throws EpsException {
		return client.getTemplates();
	}

	public Template getTemplate(String templateId) throws EpsException {

		for (Template temp : client.getTemplates()) {
			if (temp.getId().equals(templateId)) {
				return temp;
			}
		}

		return null;
	}

	// SERVICE

	public String createService(CloudService service) throws EpsException {
		return client.createService(service);
	}

	public String createServiceFromTemplate(String templateId) throws EpsException {
		return client.createServiceFromTemplate(templateId);
	}

	public void removeService(String serviceId) throws EpsException {
		client.removeService(serviceId);
	}

	public List<CloudService> getServices() throws EpsException {
		return client.getServices();
	}

	public CloudService getService(String serviceId) throws EpsException {

		for (CloudService service : client.getServices()) {
			if (service.getId().equals(serviceId)) {
				return service;
			}
		}

		return null;
	}

	public void reconfigureElasticity(String serviceId, CloudService service) throws EpsException {
		client.reconfigureElasticity(serviceId, service);
	}

	// UNIT INSTANCES

	public void putUnitInstance(String serviceId, String unitId, UnitInstance uInst)
			throws EpsException {
		client.putUnitInstance(serviceId, unitId, uInst);
	}

	public void removeUnitInstance(String serviceId, String uInstId) throws EpsException {
		client.removeUnitInstance(serviceId, uInstId);
	}

	// OSU

	public List<OfferedServiceUnit> getOsus() throws EpsException {
		return client.getOsus();
	}

	public void addOsu(OfferedServiceUnit osu) throws EpsException {
		client.addOsu(osu);
	}

	public List<OsuInstance> getOsuInstances() throws EpsException {
		return client.getOsuInstances();
	}

	public List<OsuInstance> getOsuInstancesForOsu(String osuId) throws EpsException {
		List<OsuInstance> instances = new ArrayList<>();

		for (OsuInstance instance : client.getOsuInstances()) {
			if (instance.getOsu().getId().equals(osuId)) {
				instances.add(instance);
			}
		}

		return instances;
	}

	// public List<ServiceInstance> getEpsServiceInstancesWithoutOsuInstance(String osuId) throws EpsException {
	//
	// Set<ServiceInstance> allEpsServiceInstances = getOsu(osuId).getService().getInstances();
	// Set<String> allEpsServiceInstanceWithOsuInstancIds = new HashSet<>();
	//
	// for (OsuInstance inst : getOsuInstancesForOsu(osuId)) {
	// allEpsServiceInstanceWithOsuInstancIds.add(inst.getServiceInstance().getId());
	// }
	//
	// for (Iterator<ServiceInstance> iterator = allEpsServiceInstances.iterator(); iterator.hasNext();) {
	// ServiceInstance sInst = iterator.next();
	// if (allEpsServiceInstanceWithOsuInstancIds.contains(sInst)) {
	// iterator.remove();
	// }
	// }
	// return new ArrayList<>(allEpsServiceInstances);
	// }

	public OsuInstance getOsuInstance(String osuInstanceId) throws EpsException {
		log.info("getOsuInstance " + osuInstanceId);
		for (OsuInstance instance : client.getOsuInstances()) {
			log.info(instance.getId());
			if (instance.getId().equals(osuInstanceId)) {
				return instance;
			}
		}
		return null;
	}

	public OsuInstance getOsuInstanceByServiceId(String serviceId) throws EpsException {

		for (OsuInstance instance : client.getOsuInstances()) {
			if (instance.getService() != null && instance.getService().getId().equals(serviceId)) {
				return instance;
			}
		}
		return null;
	}

	public String createOsuInstance(String osuId) throws EpsException {
		return client.createOsuInstance(osuId, null, null);
	}

	public String createOsuInstanceDynamic(String osuId, String serviceInstanceId, String osuInstanceId)
			throws EpsException {
		return client.createOsuInstance(osuId, serviceInstanceId, osuInstanceId);
	}

	public void removeOsuInatance(String osuInstanceId) throws EpsException {
		client.removeOsuInatance(osuInstanceId);
	}

	public OfferedServiceUnit getOsu(String osuId) throws EpsException {
		for (OfferedServiceUnit osu : client.getOsus()) {
			if (osu.getId().equals(osuId)) {
				return osu;
			}
		}

		return null;
	}

	public OfferedServiceUnit getOsuByTemplateId(String templateId) throws EpsException {
		for (OfferedServiceUnit osu : client.getOsus()) {
			if (osu.getServiceTemplate() != null && osu.getServiceTemplate().getId().equals(templateId)) {
				return osu;
			}
		}

		return null;
	}

	public void assignEps(String serviceId, String osuInstanceId) throws EpsException {
		client.assignEps(serviceId, osuInstanceId);
	}

	public void removeEpsAssignment(String serviceId, String osuInstanceId) throws EpsException {
		client.removeEpsAssignment(serviceId, osuInstanceId);
	}

	public boolean isOsuAssignedToService(String serviceId, String osuId) throws EpsException, JAXBException {

		boolean value = false;

		for (OsuInstance osuInstance : getService(serviceId).getSupport()) {
			if (osuInstance.getOsu().getId().equals(osuId)) {
				value = true;
			}
		}

		log.info("isOsuAssignedToInstance( serviceId={}, osu={}): {}", serviceId, osuId, value);
		return value;
	}

	public boolean isOsuInstanceAssignedToService(String serviceId, String osuInstanceId) throws EpsException {

		boolean value = false;

		for (OsuInstance osuInstance : getService(serviceId).getSupport()) {
			if (osuInstance.getId().equals(osuInstanceId)) {
				value = true;
			}
		}

		log.info("isOsuInstanceAssignedToInstance( serviceId={}, osuInstanceId={}): {}", serviceId, osuInstanceId,
				value);
		return value;
	}

	public String instanceIdOfStaticEps(String epsId) throws EpsException {

		for (OsuInstance osuInst : client.getOsuInstances()) {

			if (osuInst.getOsu().getId().equals(epsId)) {
				return osuInst.getId();
			}
		}
		return null;
	}

	public List<OsuInstance> getEpsInstances(String type) throws EpsException {

		List<OsuInstance> allEpsInstances = new ArrayList<>(getOsuInstances());

		if (ALL.equals(type)) {

		} else if (STATIC.equals(type)) {
			for (Iterator<OsuInstance> iterator = allEpsInstances.iterator(); iterator.hasNext();) {
				OsuInstance osu = iterator.next();
				if (InformationClient.isDynamicEps(osu.getOsu())) {
					iterator.remove();
				}
			}

		} else if (DYNAMIC.equals(type)) {
			for (Iterator<OsuInstance> iterator = allEpsInstances.iterator(); iterator.hasNext();) {
				OsuInstance osu = iterator.next();
				if (!InformationClient.isDynamicEps(osu.getOsu())) {
					iterator.remove();
				}
			}

		} else {
			allEpsInstances = new ArrayList<OsuInstance>();
		}

		return allEpsInstances;
	}

	public void deleteAll() throws EpsException {
		client.deleteAll();
	}

	public void setBaseUri(URI baseUri) {
		client.setBaseUri(baseUri);
	}

	public URI getBaseUri() {
		return client.getBaseUri();
	}

	public boolean isServiceOfDynamicEps(String serviceId) throws EpsException {

		OsuInstance osuInstance = getOsuInstanceByServiceId(serviceId);

		if (osuInstance == null) {
			return false;
		}

		return isDynamicEps(osuInstance.getOsu());
	}

	public static boolean isDynamicEps(OfferedServiceUnit osu) throws EpsException {

		if (osu != null && osu.getType().equals(OsuType.EPS.toString()) && osu.getServiceTemplate() != null) {
			return true;
		} else {
			return false;
		}

	}

	public String createDynamicEpsInstance(String epsId) throws EpsException {

		// log.info("epsId {}", epsId);
		//
		// String instanceId = createOsuInstance(epsId);
		// log.info("instanceId {}", instanceId);
		//
		// String templateId = getOsu(epsId).getServiceTemplate().getId();
		// log.info("templateId {}", templateId);
		//
		// String serviceId = client.createServiceFromTemplate(templateId);

		String serviceId = createOsuInstance(epsId);

		return serviceId;
	}

}
