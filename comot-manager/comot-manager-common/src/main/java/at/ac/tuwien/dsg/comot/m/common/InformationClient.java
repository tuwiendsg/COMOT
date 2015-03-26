package at.ac.tuwien.dsg.comot.m.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public class InformationClient {

	protected final Logger log = LoggerFactory.getLogger(getClass());

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

	public OfferedServiceUnit getOsu(String osuId) throws EpsException {
		for (OfferedServiceUnit osu : client.getOsus()) {
			if (osu.getId().equals(osuId)) {
				return osu;
			}
		}

		return null;
	}

	public void addOsu(OfferedServiceUnit osu) throws EpsException {
		client.addOsu(osu);
	}

	public void assignEps(String serviceId, String instanceId, String osuInstanceId) throws EpsException {
		client.assignEps(instanceId, osuInstanceId);
	}

	public Map<String, String> getInstancesHavingThisOsuAssigned(String osuInstanceId) throws EpsException {
		Map<String, String> instances = new HashMap<>();

		for (CloudService service : client.getServices()) {
			for (ServiceInstance instance : service.getInstances()) {
				for (OfferedServiceUnit osu : instance.getSupport()) {
					if (osu.getId().equals(osuInstanceId)) {
						instances.put(instance.getId(), service.getId());
					}
				}
			}
		}

		return instances;
	}

	public void removeEpsAssignment(String instanceId, String osuInstanceId) throws EpsException {
		client.removeEpsAssignment(instanceId, osuInstanceId);
	}

	public Set<OfferedServiceUnit> getSupportingServices(String instanceId)
			throws ClassNotFoundException, IOException, EpsException {

		ServiceInstance instance = client.getServiceInstance(instanceId).getInstancesList().get(0);
		Set<OfferedServiceUnit> result;
		if (instance != null) {
			result = (Set<OfferedServiceUnit>) Utils.deepCopy(instance.getSupport());
		} else {
			result = new HashSet<OfferedServiceUnit>();
		}
		return result;

	}

	public boolean isOsuAssignedToInstance(String instanceId, String osuId) throws EpsException {
		CloudService serv = client.getServiceInstance(instanceId);
		for (OfferedServiceUnit osu : client.getServiceInstance(instanceId).getInstancesList().get(0).getSupport()) {
			try {
				log.info("serv {}", Utils.asJsonString(serv));
			} catch (JAXBException e) {
				e.printStackTrace();
			}
			if (osu.getId().equals(osuId)) {
				log.info("isOsuAssignedToInstance( instanceId={}, osuId={}): true", instanceId,
						osuId);
				return true;
			}
		}

		log.info("isOsuAssignedToInstance( instanceId={}, osuId={}): false", instanceId, osuId);
		return false;
	}

	public void deeteAll() throws EpsException {
		client.deeteAll();
	}

}
