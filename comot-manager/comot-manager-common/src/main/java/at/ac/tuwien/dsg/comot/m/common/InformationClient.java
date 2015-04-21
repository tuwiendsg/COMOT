package at.ac.tuwien.dsg.comot.m.common;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;
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

	// SERVICE

	public String createService(CloudService service) throws EpsException {
		return client.createService(service);
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

	public List<CloudService> getServices() throws EpsException {
		return client.getServices();
	}

	// SERVICE INSTANCE

	public String createServiceInstance(String serviceId) throws EpsException {
		return client.createServiceInstance(serviceId);
	}

	public void removeServiceInstance(String serviceId, String instanceId) throws EpsException {
		client.removeServiceInstance(serviceId, instanceId);
	}

	public CloudService getServiceInstance(String instanceId) throws EpsException {
		return client.getServiceInstance(instanceId);
	}

	public CloudService getServiceInstance(String serviceId, String instanceId) throws EpsException {
		return client.getServiceInstance(instanceId);
	}

	public Map<String, List<String>> getAllInstanceIds() throws EpsException {

		Map<String, List<String>> serviceMap = new HashMap<>();
		List<String> tempList;

		for (CloudService service : client.getServices()) {
			tempList = new ArrayList<String>();
			serviceMap.put(service.getId(), tempList);
			for (ServiceInstance instance : service.getInstances()) {
				tempList.add(instance.getId());
			}
		}

		return serviceMap;
	}

	// UNIT INSTANCES

	public void putUnitInstance(String serviceId, String csInstanceId, String unitId, UnitInstance uInst)
			throws EpsException {
		client.putUnitInstance(serviceId, csInstanceId, unitId, uInst);
	}

	public void removeUnitInstance(String serviceId, String csInstanceId, String uInstId) throws EpsException {
		client.removeUnitInstance(serviceId, csInstanceId, uInstId);
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

	public List<ServiceInstance> getEpsServiceInstancesWithoutOsuInstance(String osuId) throws EpsException {

		Set<ServiceInstance> allEpsServiceInstances = getOsu(osuId).getService().getInstances();
		Set<String> allEpsServiceInstanceWithOsuInstancIds = new HashSet<>();

		for (OsuInstance inst : getOsuInstancesForOsu(osuId)) {
			allEpsServiceInstanceWithOsuInstancIds.add(inst.getServiceInstance().getId());
		}

		for (Iterator<ServiceInstance> iterator = allEpsServiceInstances.iterator(); iterator.hasNext();) {
			ServiceInstance sInst = iterator.next();
			if (allEpsServiceInstanceWithOsuInstancIds.contains(sInst)) {
				iterator.remove();
			}
		}
		return new ArrayList<>(allEpsServiceInstances);
	}

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

	public OfferedServiceUnit getOsuByServiceId(String serviceId) throws EpsException {
		for (OfferedServiceUnit osu : client.getOsus()) {
			if (osu.getService() != null && osu.getService().getId().equals(serviceId)) {
				return osu;
			}
		}

		return null;
	}

	public void assignEps(String serviceId, String instanceId, String osuInstanceId) throws EpsException {
		client.assignEps(instanceId, osuInstanceId);
	}

	public void removeEpsAssignment(String instanceId, String osuInstanceId) throws EpsException {
		client.removeEpsAssignment(instanceId, osuInstanceId);
	}

	public Set<OsuInstance> getSupportingServices(String instanceId)
			throws ClassNotFoundException, IOException, EpsException {

		ServiceInstance instance = client.getServiceInstance(instanceId).getInstancesList().get(0);
		Set<OsuInstance> result = instance.getSupport();
		return result;

	}

	public boolean isOsuAssignedToInstance(String instanceId, String osuId) throws EpsException, JAXBException {

		boolean value = false;
		CloudService serv = client.getServiceInstance(instanceId);

		for (OsuInstance osuInstance : serv.getInstancesList().get(0).getSupport()) {
			if (osuInstance.getOsu().getId().equals(osuId)) {
				value = true;
			}
		}

		log.info("isOsuAssignedToInstance( instanceId={}, osu={}): {}", instanceId, osuId, value);
		return value;
	}

	public boolean isOsuInstanceAssignedToInstance(String instanceId, String osuInstanceId) throws EpsException {

		boolean value = false;
		CloudService serv = client.getServiceInstance(instanceId);

		for (OsuInstance osuInstance : serv.getInstancesList().get(0).getSupport()) {
			if (osuInstance.getId().equals(osuInstanceId)) {
				value = true;
			}
		}

		log.info("isOsuInstanceAssignedToInstance( instanceId={}, osuInstanceId={}): {}", instanceId, osuInstanceId,
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

	public List<OsuInstance> getElasticPlatformServicesInstances(String type) throws EpsException {

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

	public void deeteAll() throws EpsException {
		client.deleteAll();
	}

	public void setBaseUri(URI baseUri) {
		client.setBaseUri(baseUri);
	}

	public URI getBaseUri() {
		return client.getBaseUri();
	}

	public boolean isServiceOfDynamicEps(String serviceId) throws EpsException {

		OfferedServiceUnit osu = getOsuByServiceId(serviceId);

		return isDynamicEps(osu);
	}

	public static boolean isDynamicEps(OfferedServiceUnit osu) throws EpsException {

		if (osu != null && osu.getType().equals(OsuType.EPS.toString()) && osu.getService() != null) {
			return true;
		} else {
			return false;
		}

	}

}
