package at.ac.tuwien.dsg.comot.m.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Modifier;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
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
	protected Map<String, OsuInstance> osuInstances = Collections
			.synchronizedMap(new HashMap<String, OsuInstance>());

	protected int instanceItarator = 0;
	protected int osuInstanceItarator = 0;

	public void deleteAll() {
		services = Collections.synchronizedMap(new HashMap<String, CloudService>());
		osus = Collections.synchronizedMap(new HashMap<String, OfferedServiceUnit>());
		osuInstances = Collections.synchronizedMap(new HashMap<String, OsuInstance>());
		instanceItarator = 0;
		osuInstanceItarator = 0;
	}

	protected synchronized String getInstanceId(String serviceId) {
		return serviceId + "_" + ++instanceItarator;
	}

	protected synchronized String getOsuInstanceId(String osuId) {
		return osuId + "_" + ++osuInstanceItarator;
	}

	public String createService(CloudService service) throws ClassNotFoundException, IOException {

		service.setDateCreated(System.currentTimeMillis());
		services.put(service.getId(), service);

		return service.getId();
	}

	public CloudService getService(String serviceId) {
		return services.get(serviceId);
	}

	public void reconfigureElasticity(String serviceId, CloudService elConfig) {

		try {
			log.info("input {}", Utils.asJsonString(elConfig));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CloudService service = services.get(serviceId);

		Modifier.replaceSyblDirectives(elConfig, service);

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

		for (Iterator<UnitInstance> i = _getServiceInstance(instanceId).getUnitInstances().iterator(); i.hasNext();) {
			UnitInstance uInst = i.next();
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

	public List<CloudService> getServices() {
		return new ArrayList<CloudService>(services.values());
	}

	// OSU

	public void addOsu(OfferedServiceUnit osu) {

		if (osu.getService() != null) {
			osu.setService(services.get(osu.getService().getId()));
		}

		osus.put(osu.getId(), osu);
	}

	//
	public String createOsuInstance(String osuId) {

		OfferedServiceUnit osu = osus.get(osuId);
		String osuInstanceId = getOsuInstanceId(osuId);
		OsuInstance osuInstance = new OsuInstance(osuInstanceId, osu);
		osuInstances.put(osuInstanceId, osuInstance);
		log.info("createOsuInstance(osuId={}):{}", osuId, osuInstanceId);
		return osuInstanceId;
	}

	public String createOsuInstanceDynamic(String osuId, String instanceId, String osuInstanceId) {

		OfferedServiceUnit osu = osus.get(osuId);
		OsuInstance osuInstance = new OsuInstance(osuInstanceId, osu);
		osuInstances.put(osuInstanceId, osuInstance);
		log.info("createOsuInstance(osuId={}):{}", osuId, osuInstanceId);

		osuInstances.get(osuInstanceId).setServiceInstance(_getServiceInstance(instanceId));
		return osuInstanceId;
	}

	public void removeOsuInatance(String osuInstanceId) {
		osuInstances.remove(osuInstanceId);
	}

	public void assignSupportingService(String instanceId, String osuInstanceId) {

		if (isOsuAssignedToInstance(instanceId, osuInstanceId)) {
			return;
		}

		ServiceInstance instance = _getServiceInstance(instanceId);
		instance.getSupport().add(osuInstances.get(osuInstanceId));
	}

	public void removeAssignmentOfSupportingOsu(String instanceId, String osuInstanceId) {

		if (isOsuAssignedToInstance(instanceId, osuInstanceId)) {

			ServiceInstance instance = _getServiceInstance(instanceId);

			for (OsuInstance osuInstance : instance.getSupport()) {
				if (osuInstance.getId().equals(osuInstanceId)) {
					instance.getSupport().remove(osuInstance);
					return;
				}
			}

		}
	}

	private boolean isOsuAssignedToInstance(String instanceId, String osuInstanceId) {

		boolean value = false;

		for (OsuInstance osuInstance : _getServiceInstance(instanceId).getSupport()) {
			if (osuInstance.getId().equals(osuInstanceId)) {
				value = true;
			}
		}

		log.info("isOsuAssignedToInstance( instanceId={}, osuInstanceId={}): {}", instanceId, osuInstanceId, value);
		return value;
	}

	public List<OfferedServiceUnit> getOsus() {
		return new ArrayList<OfferedServiceUnit>(osus.values());
	}

	public List<OsuInstance> getOsusInstances() {
		return new ArrayList<OsuInstance>(osuInstances.values());
	}

}
