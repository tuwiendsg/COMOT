package at.ac.tuwien.dsg.comot.m.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@Component
public class InformationServiceMock {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@javax.annotation.Resource
	public Environment env;

	protected Map<String, CloudService> services = Collections.synchronizedMap(new HashMap<String, CloudService>());
	protected Map<String, OfferedServiceUnit> osus = Collections
			.synchronizedMap(new HashMap<String, OfferedServiceUnit>());

	protected int instanceItarator = 0;

	public void deleteAll() {
		services = Collections.synchronizedMap(new HashMap<String, CloudService>());
		osus = Collections.synchronizedMap(new HashMap<String, OfferedServiceUnit>());
		instanceItarator = 0;
	}

	protected synchronized String getInstanceId(String serviceId) {
		return serviceId + "_" + ++instanceItarator;
	}

	public String createService(CloudService service) throws ClassNotFoundException, IOException {

		service = (CloudService) Utils.deepCopy(service);
		service.setDateCreated(new Date());
		services.put(service.getId(), service);

		return service.getId();
	}

	public CloudService getService(String serviceId) {
		return services.get(serviceId);
	}

	public String createServiceInstance(String serviceId) {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("There is no service '" + serviceId + "'");
		}

		CloudService service = services.get(serviceId);
		ServiceInstance instance = service.createServiceInstance(getInstanceId(serviceId));
		return instance.getId();
	}

	public void removeServiceInstance(String serviceId, String instanceId) {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("There is no service '" + serviceId + "'");
		}

		CloudService service = services.get(serviceId);

		for (UnitInstance uInst : _getServiceInstance(instanceId).getUnitInstances()) {
			removeUnitInstance(serviceId, instanceId, uInst.getId());
		}

		for (ServiceInstance instance : service.getInstances()) {
			if (instance.getId().equals(instanceId)) {
				service.getInstances().remove(instance);
				break;
			}
		}

	}

	public void putUnitInstance(String serviceId, String csInstanceId, String unitId, UnitInstance uInst) {

		// try {
		// log.info("services {}", services.get(serviceId));
		// log.info("servicesEEEEE {}", Utils.asJsonString(services.get(serviceId)));
		// } catch (JAXBException e) {
		// e.printStackTrace();
		// }
		//
		// log.info("uInst {}",uInst );
		// try {
		// log.info("uInstEEEEE {}", Utils.asJsonString(uInst));
		// } catch (JAXBException e) {
		// e.printStackTrace();
		// }
		//
		// log.info("uInst.getId() {}", uInst.getId());
		//
		//
		// log.info("nav.getUnitFor(uInst.getId()) {}", nav.getUnitFor(uInst.getId()) );

		removeUnitInstance(serviceId, csInstanceId, uInst.getId());
		addUnitInstance(serviceId, csInstanceId, unitId, uInst);

	}

	public void addUnitInstance(String serviceId, String csInstanceId, String unitId, UnitInstance uInst) {

		Navigator nav = new Navigator(services.get(serviceId));
		nav.getUnit(unitId).addUnitInstance(uInst);

		ServiceInstance instance = _getServiceInstance(csInstanceId);
		instance.getUnitInstances().add(uInst);
	}

	public void removeUnitInstance(String serviceId, String csInstanceId, String uInstId) {

		Navigator nav = new Navigator(services.get(serviceId));
		UnitInstance uInst = nav.getInstance(uInstId);

		if (uInst != null) {
			nav.getUnitFor(uInstId).getInstances().remove(uInst);

			ServiceInstance instance = _getServiceInstance(csInstanceId);
			instance.getUnitInstances().remove(uInst);
		}
	}

	public void addOsu(OfferedServiceUnit osu) {
		osus.put(osu.getId(), osu);
	}

	public void assignSupportingService(String instanceId, String osuInstanceId) {

		if (isOsuAssignedToInstance(instanceId, osuInstanceId)) {
			return;
		}

		ServiceInstance instance = _getServiceInstance(instanceId);
		instance.getSupport().add(osus.get(osuInstanceId));
	}

	public void removeAssignmentOfSupportingOsu(String instanceId, String osuInstanceId) {

		if (isOsuAssignedToInstance(instanceId, osuInstanceId)) {

			ServiceInstance instance = _getServiceInstance(instanceId);

			for (OfferedServiceUnit osu : instance.getSupport()) {
				if (osu.getId().equals(osuInstanceId)) {
					instance.getSupport().remove(osu);
					return;
				}
			}

		}
	}

	private boolean isOsuAssignedToInstance(String instanceId, String osuId) {

		for (OfferedServiceUnit osu : _getServiceInstance(instanceId).getSupport()) {
			if (osu.getId().equals(osuId)) {
				log.info("isOsuAssignedToInstance( instanceId={}, osuId={}): true", instanceId,
						osuId);
				return true;
			}
		}

		log.info("isOsuAssignedToInstance( instanceId={}, osuId={}): false", instanceId, osuId);
		return false;
	}

	private ServiceInstance _getServiceInstance(String instanceId) {
		for (CloudService service : services.values()) {
			for (ServiceInstance instance : service.getInstances()) {
				if (instance.getId().equals(instanceId)) {
					return instance;
				}
			}
		}
		return null;
	}

	public CloudService getServiceInstance(String instanceId) throws ClassNotFoundException,
			IOException {

		for (CloudService service : services.values()) {
			for (ServiceInstance servInst : service.getInstances()) {
				if (servInst.getId().equals(instanceId)) {
					return getServiceInstance(service.getId(), instanceId);
				}
			}
		}

		return null;
	}

	public CloudService getServiceInstance(String serviceId, String instanceId) throws ClassNotFoundException,
			IOException {

		boolean isIncluded;

		CloudService copyServ = (CloudService) Utils.deepCopy(services.get(serviceId));
		ServiceInstance servInst = null;

		for (Iterator<ServiceInstance> i = copyServ.getInstances().iterator(); i.hasNext();) {
			ServiceInstance tempInst = i.next();

			if (tempInst.getId().equals(instanceId)) {
				servInst = tempInst;
			} else {
				i.remove();
			}
		}
		if (servInst == null) {
			return null;
		}

		Navigator nav = new Navigator(copyServ);

		for (ServiceUnit unit : nav.getAllUnits()) {
			for (Iterator<UnitInstance> i = unit.getInstances().iterator(); i.hasNext();) {
				UnitInstance uInst = i.next();

				isIncluded = false;

				for (UnitInstance uuInst : servInst.getUnitInstances()) {
					if (uuInst.getId().equals(uInst.getId())) {
						isIncluded = true;
					}
				}
				if (!isIncluded) {
					i.remove();
				}
			}
		}

		try {
			log.debug("getServiceInstance(): {}", Utils.asJsonString(copyServ));
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return copyServ;
	}

	public List<OfferedServiceUnit> getOsus() {
		return new ArrayList<OfferedServiceUnit>(osus.values());
	}

	public List<CloudService> getServices() {
		return new ArrayList<CloudService>(services.values());
	}

}
