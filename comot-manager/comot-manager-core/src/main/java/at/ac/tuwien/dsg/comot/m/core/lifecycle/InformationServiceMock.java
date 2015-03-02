package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.DeploymentAdapter;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.RecordingAdapter;
import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;

@Component
public class InformationServiceMock {

	protected final Logger log = LoggerFactory.getLogger(getClass());

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

	@PostConstruct
	public void setUp() {

		// SALSA
		Resource resource = new Resource(PUBLIC_INSTANCE, new ResourceOrQualityType(TYPE_STATIC_SERVICE));
		resource.hasResource(new Resource(DeploymentAdapter.class.getCanonicalName(),
				new ResourceOrQualityType(ADAPTER_CLASS)));
		resource.hasResource(new Resource(AppContextEps.SALSA_IP, new ResourceOrQualityType(IP)));
		resource.hasResource(new Resource(AppContextEps.SALSA_PORT.toString(), new ResourceOrQualityType(PORT)));

		OfferedServiceUnit deployment = new OfferedServiceUnit();
		deployment.setId(SALSA_SERVICE_PUBLIC_ID);
		deployment.hasResource(resource);

		// MELA

		Resource resource2 = new Resource(PUBLIC_INSTANCE, new ResourceOrQualityType(TYPE_STATIC_SERVICE));
		resource2.hasResource(new Resource("TODO", new ResourceOrQualityType(ADAPTER_CLASS)));
		resource2.hasResource(new Resource("128.130.172.215", new ResourceOrQualityType(IP)));
		resource2.hasResource(new Resource("8180", new ResourceOrQualityType(PORT)));

		OfferedServiceUnit monitoring = new OfferedServiceUnit();
		monitoring.setId(MELA_SERVICE_PUBLIC_ID);
		monitoring.hasResource(resource2);
		monitoring.hasPrimitiveOperation(new PrimitiveOperation("Set Metric Composition Rules", SET_MCR));

		// RSYBL

		Resource resource3 = new Resource(PUBLIC_INSTANCE, new ResourceOrQualityType(TYPE_STATIC_SERVICE));
		resource3.hasResource(new Resource("TODO", new ResourceOrQualityType(ADAPTER_CLASS)));
		resource3.hasResource(new Resource("128.130.172.215", new ResourceOrQualityType(IP)));
		resource3.hasResource(new Resource("8020", new ResourceOrQualityType(PORT)));

		OfferedServiceUnit control = new OfferedServiceUnit();
		control.setId(RSYBL_SERVICE_PUBLIC_ID);
		control.hasResource(resource3);
		control.hasPrimitiveOperation(new PrimitiveOperation("Set Metric Composition Rules", SET_MCR));
		control.hasPrimitiveOperation(new PrimitiveOperation("Set Effects", SET_EFFECTS));

		// RECORDER

		Resource resource4 = new Resource(PUBLIC_INSTANCE, new ResourceOrQualityType(TYPE_STATIC_SERVICE));
		resource4.hasResource(new Resource(RecordingAdapter.class.getCanonicalName(), new ResourceOrQualityType(
				ADAPTER_CLASS)));

		OfferedServiceUnit recorder = new OfferedServiceUnit();
		control.setId(RECORDER_SERVICE);
		control.hasResource(resource4);

		osus.put(SALSA_SERVICE_PUBLIC_ID, deployment);
		osus.put(RECORDER_SERVICE, recorder);
		// osus.put(MELA_SERVICE_PUBLIC_ID, monitoring);
		// osus.put(RSYBL_SERVICE_PUBLIC_ID, control);
	}

	public String createService(CloudService service) {
		services.put(service.getId(), service);

		return service.getId();
	}

	public String createServiceInstance(String serviceId) {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("There is no service '" + serviceId + "'");
		}

		CloudService service = services.get(serviceId);
		ServiceInstance instance = service.createServiceInstance(serviceId + "_" + UUID.randomUUID().toString());

		return instance.getId();
	}

	public void assignSupportingService(String instanceId, String osuInstanceId) {

		for (CloudService service : services.values()) {
			for (ServiceInstance instance : service.getInstances()) {
				if (instance.getId().equals(instanceId)) {
					instance.getSupport().add(osus.get(osuInstanceId));
				}
			}
		}
	}

	// public void updateRuntimeAndDevelInfo(String instanceId, CloudService service){
	//
	// services.get(service.getId())
	// }

	public Set<OfferedServiceUnit> getSupportingServices(String instanceId) {

		for (CloudService service : services.values()) {
			for (ServiceInstance instance : service.getInstances()) {
				if (instance.getId().equals(instanceId)) {
					return instance.getSupport();
				}
			}
		}

		return new HashSet<OfferedServiceUnit>();
	}

	public CloudService getService(String serviceId) {
		return services.get(serviceId);
	}

	public CloudService getServiceInstance(String instanceId) throws ClassNotFoundException, IOException {

		for (CloudService service : services.values()) {
			for (ServiceInstance instance : service.getInstances()) {

				if (instance.getId().equals(instanceId)) {
					CloudService copy = (CloudService) Utils.deepCopy(service);
					copy.getInstances().clear();
					copy.getInstances().add(instance);

					// TODO clean also unitInstances

					return copy;
				}
			}
		}

		return null;
	}

	public Map<String, OfferedServiceUnit> getOsus() {
		return osus;
	}

	public Map<String, CloudService> getServices() {
		return services;
	}

}
