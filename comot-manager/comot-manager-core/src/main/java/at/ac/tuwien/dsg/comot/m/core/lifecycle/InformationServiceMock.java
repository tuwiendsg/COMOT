package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.DeploymentAdapter;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.RecordingAdapter;
import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@Component
public class InformationServiceMock {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ToscaMapper mapperTosca;

	public static final String TYPE_ACTION = "TYPE_ACTION";

	public static final String SALSA_SERVICE_PUBLIC_ID = "SALSA_SERVICE";
	public static final String MELA_SERVICE_PUBLIC_ID = "MELA_SERVICE";
	public static final String RSYBL_SERVICE_PUBLIC_ID = "RSYBL_SERVICE";
	public static final String RECORDER_SERVICE = "RECORDER_SERVICE";

	public static final String PUBLIC_INSTANCE = "PUBLIC_INSTANCE";
	public static final String TYPE_STATIC_SERVICE = "TYPE_STATIC_SERVICE";

	public static final String ADAPTER_CLASS = "ADAPTER_CLASS";
	public static final String IP = "IP";
	public static final String PORT = "PORT";

	// custom events
	public static final String SET_MCR = "SET_MCR";
	public static final String SET_EFFECTS = "SET_EFFECTS";

	protected Map<String, CloudService> services = new HashMap<>();
	protected Map<String, OfferedServiceUnit> osus = new HashMap<>();

	protected int instanceItarator = 0;

	@PostConstruct
	public void setUpTestData() {

		// SALSA
		Resource resource = new Resource(InformationServiceMock.PUBLIC_INSTANCE, new ResourceOrQualityType(
				InformationServiceMock.TYPE_STATIC_SERVICE));
		resource.hasResource(new Resource(DeploymentAdapter.class.getCanonicalName(),
				new ResourceOrQualityType(InformationServiceMock.ADAPTER_CLASS)));
		resource.hasResource(new Resource(AppContextEps.SALSA_IP, new ResourceOrQualityType(InformationServiceMock.IP)));
		resource.hasResource(new Resource(AppContextEps.SALSA_PORT.toString(), new ResourceOrQualityType(
				InformationServiceMock.PORT)));

		OfferedServiceUnit deployment = new OfferedServiceUnit();
		deployment.setId(InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);
		deployment.hasResource(resource);

		// MELA

		Resource resource2 = new Resource(InformationServiceMock.PUBLIC_INSTANCE, new ResourceOrQualityType(
				InformationServiceMock.TYPE_STATIC_SERVICE));
		resource2.hasResource(new Resource("TODO", new ResourceOrQualityType(InformationServiceMock.ADAPTER_CLASS)));
		resource2.hasResource(new Resource("128.130.172.215", new ResourceOrQualityType(InformationServiceMock.IP)));
		resource2.hasResource(new Resource("8180", new ResourceOrQualityType(InformationServiceMock.PORT)));

		OfferedServiceUnit monitoring = new OfferedServiceUnit();
		monitoring.setId(InformationServiceMock.MELA_SERVICE_PUBLIC_ID);
		monitoring.hasResource(resource2);
		monitoring.hasPrimitiveOperation(new PrimitiveOperation("Set Metric Composition Rules",
				InformationServiceMock.SET_MCR));

		// RSYBL

		Resource resource3 = new Resource(InformationServiceMock.PUBLIC_INSTANCE, new ResourceOrQualityType(
				InformationServiceMock.TYPE_STATIC_SERVICE));
		resource3.hasResource(new Resource("TODO", new ResourceOrQualityType(InformationServiceMock.ADAPTER_CLASS)));
		resource3.hasResource(new Resource("128.130.172.215", new ResourceOrQualityType(InformationServiceMock.IP)));
		resource3.hasResource(new Resource("8020", new ResourceOrQualityType(InformationServiceMock.PORT)));

		OfferedServiceUnit control = new OfferedServiceUnit();
		control.setId(InformationServiceMock.RSYBL_SERVICE_PUBLIC_ID);
		control.hasResource(resource3);
		control.hasPrimitiveOperation(new PrimitiveOperation("Set Metric Composition Rules",
				InformationServiceMock.SET_MCR));
		control.hasPrimitiveOperation(new PrimitiveOperation("Set Effects", InformationServiceMock.SET_EFFECTS));

		// RECORDER

		Resource resource4 = new Resource(InformationServiceMock.PUBLIC_INSTANCE, new ResourceOrQualityType(
				InformationServiceMock.TYPE_STATIC_SERVICE));
		resource4.hasResource(new Resource(RecordingAdapter.class.getCanonicalName(), new ResourceOrQualityType(
				InformationServiceMock.ADAPTER_CLASS)));

		OfferedServiceUnit recorder = new OfferedServiceUnit();
		recorder.setId(InformationServiceMock.RECORDER_SERVICE);
		recorder.hasResource(resource4);

		this.addOsu(deployment);
		this.addOsu(recorder);
		// this.addOsu(monitoring);
		// this.addOsu(control);

		try {
			CloudService service1 = mapperTosca.createModel(
					UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml"));

			String serviceId = this.createService(service1);
			// String instanceId = this.createServiceInstance(service1.getId());
			// this.assignSupportingService(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

		try {
			this.createService(
					mapperTosca.createModel(
							UtilsCs.loadTosca("./../resources/test/daas_m2m_fromSalsa.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

	}

	protected synchronized String getInstanceId(String serviceId) {
		return serviceId + "_" + ++instanceItarator;
	}

	public String createService(CloudService service) {
		service.setDateCreated(new Date());
		services.put(service.getId(), service);

		return service.getId();
	}

	public String createServiceInstance(String serviceId) {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("There is no service '" + serviceId + "'");
		}

		CloudService service = services.get(serviceId);
		ServiceInstance instance = service.createServiceInstance(getInstanceId(serviceId));
		return instance.getId();
	}

	public void addUnitInstance(String serviceId, String csInstanceId, String unitId, UnitInstance uInst) {

		Navigator nav = new Navigator(services.get(serviceId));
		nav.getUnit(unitId).addUnitInstance(uInst);

		ServiceInstance instance = _getServiceInstance(serviceId, csInstanceId);
		instance.getUnitInstances().add(uInst);
	}

	public void removeUnitInstance(String serviceId, String csInstanceId, String uInstId) {

		Navigator nav = new Navigator(services.get(serviceId));
		UnitInstance uInst = nav.getInstance(uInstId);

		nav.getUnitFor(uInstId).getInstances().remove(uInst);

		ServiceInstance instance = _getServiceInstance(serviceId, csInstanceId);
		instance.getUnitInstances().remove(uInst);
	}

	public void addOsu(OfferedServiceUnit osu) {
		osus.put(osu.getId(), osu);
	}

	public void assignSupportingService(String serviceId, String instanceId, String osuInstanceId) {

		ServiceInstance instance = _getServiceInstance(serviceId, instanceId);
		instance.getSupport().add(osus.get(osuInstanceId));

	}

	public Map<String, String> getInstancesHavingThisOsuAssigned(String osuInstanceId) {
		Map<String, String> instances = new HashMap<>();

		for (CloudService service : services.values()) {
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

	public void removeAssignmentOfSupportingOsu(String serviceId, String instanceId, String osuInstanceId) {

		ServiceInstance instance = _getServiceInstance(serviceId, instanceId);

		for (OfferedServiceUnit osu : instance.getSupport()) {
			if (osu.getId().equals(osuInstanceId)) {
				instance.getSupport().remove(osu);
				return;
			}
		}

	}

	// public void updateRuntimeAndDevelInfo(String instanceId, CloudService service){
	//
	// services.get(service.getId())
	// }

	public Set<OfferedServiceUnit> getSupportingServices(String serviceId, String instanceId)
			throws ClassNotFoundException, IOException {

		ServiceInstance instance = _getServiceInstance(serviceId, instanceId);
		Set<OfferedServiceUnit> result;
		if (instance != null) {
			result = (Set<OfferedServiceUnit>) Utils.deepCopy(instance.getSupport());
		} else {
			result = new HashSet<OfferedServiceUnit>();
		}
		return result;

	}

	public boolean isOsuAssignedToInstance(String serviceId, String instanceId, String osuId)
			throws ClassNotFoundException, IOException {

		for (OfferedServiceUnit osu : _getServiceInstance(serviceId, instanceId).getSupport()) {
			if (osu.getId().equals(osuId)) {
				log.info("isOsuAssignedToInstance(serviceId={}, instanceId={}, osuId={}): true", serviceId, instanceId,
						osuId);
				return true;
			}
		}

		log.info("isOsuAssignedToInstance(serviceId={}, instanceId={}, osuId={}): false", serviceId, instanceId, osuId);
		return false;
	}

	public CloudService getService(String serviceId) {
		return services.get(serviceId);
	}

	private ServiceInstance _getServiceInstance(String serviceId, String instanceId) {
		for (ServiceInstance instance : services.get(serviceId).getInstances()) {
			if (instance.getId().equals(instanceId)) {
				return instance;
			}
		}
		return null;
	}

	public CloudService getServiceInstance(String serviceId, String instanceId) throws ClassNotFoundException,
			IOException {

		boolean isIncluded;

		CloudService service = services.get(serviceId);
		for (ServiceInstance servInst : service.getInstances()) {

			if (servInst.getId().equals(instanceId)) {
				CloudService copy = (CloudService) Utils.deepCopy(service);
				ServiceInstance copyInstance = (ServiceInstance) Utils.deepCopy(servInst);

				copy.getInstances().clear();
				copy.getInstances().add(copyInstance);

				Navigator nav = new Navigator(copy);

				for (ServiceUnit unit : nav.getAllUnits()) {
					for (Iterator<UnitInstance> i = unit.getInstances().iterator(); i.hasNext();) {
						UnitInstance uInst = i.next();

						isIncluded = false;

						for (UnitInstance uuInst : copyInstance.getUnitInstances()) {
							if (uuInst.getId().equals(uInst.getId())) {
								isIncluded = true;
							}
						}
						if (!isIncluded) {
							i.remove();
						}
					}
				}
				return copy;
			}
		}
		return null;
	}

	public Map<String, List<String>> getAllInstanceIds() {

		Map<String, List<String>> serviceMap = new HashMap<>();
		List<String> tempList;

		for (CloudService service : services.values()) {
			tempList = new ArrayList<String>();
			serviceMap.put(service.getId(), tempList);
			for (ServiceInstance instance : service.getInstances()) {
				tempList.add(instance.getId());
			}
		}

		return serviceMap;
	}

	public Map<String, OfferedServiceUnit> getOsus() {
		return osus;
	}

	public Map<String, CloudService> getServices() {
		return services;
	}

	public List<CloudService> getServicesWithoutRuntimeInfo() throws ClassNotFoundException, IOException {

		List<CloudService> tempSet = new ArrayList<>();
		CloudService temp;
		Navigator nav;

		for (CloudService service : services.values()) {
			temp = (CloudService) Utils.deepCopy(service);
			temp.setInstances(new HashSet<ServiceInstance>());
			nav = new Navigator(temp);
			tempSet.add(temp);

			for (ServiceUnit unit : nav.getAllUnits()) {
				unit.setInstances(new HashSet<UnitInstance>());
			}
		}

		return tempSet;
	}
}
