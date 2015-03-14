/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.EpsAction;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class Coordinator {

	private static final Logger log = LoggerFactory.getLogger(Coordinator.class);

	public static final String USER_ID = "Some User";

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected DeploymentClient deployment;

	@PostConstruct
	public void setUp() {
		insertRunningService("HelloElasticity_VM");
	}

	public String createCloudService(CloudService service) throws ClassNotFoundException, IOException {

		String serviceId = infoService.createService(service);
		return serviceId;
	}

	public String createServiceInstance(String serviceId) throws IOException, JAXBException, ClassNotFoundException {

		String instanceId = infoService.createServiceInstance(serviceId);

		LifeCycleEvent event = new LifeCycleEvent(serviceId, instanceId, serviceId, Action.CREATED,
				USER_ID, null);
		lcManager.executeAction(event);

		return instanceId;
	}

	public void startServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException,
			ClassNotFoundException {

		LifeCycleEvent event = new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STARTED, USER_ID, null);
		lcManager.executeAction(event);

	}

	public void stopServiceInstance(String serviceId, String instanceId)
			throws IOException, JAXBException, ClassNotFoundException {

		LifeCycleEvent event = new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STOPPED, USER_ID, null);
		lcManager.executeAction(event);

	}

	public void removeServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException,
			ClassNotFoundException {

		infoService.removeServiceInstance(serviceId, instanceId);

		LifeCycleEvent event = new LifeCycleEvent(serviceId, instanceId, serviceId, Action.REMOVED, USER_ID, null);
		lcManager.executeAction(event);

	}

	public void assignSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		infoService.assignSupportingService(serviceId, instanceId, osuInstanceId);

		CustomEvent event = new CustomEvent(serviceId, instanceId, serviceId, EpsAction.EPS_ASSIGNED.toString(),
				USER_ID, osuInstanceId, null); // TODu remove osuInstanceId from the optional msg
		lcManager.executeAction(event);

	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		infoService.removeAssignmentOfSupportingOsu(serviceId, instanceId, osuInstanceId);

		CustomEvent event = new CustomEvent(serviceId, instanceId, serviceId,
				EpsAction.EPS_ASSIGNMENT_REMOVED.toString(), USER_ID, osuInstanceId, null);
		lcManager.executeAction(event);

	}

	public void triggerCustomEvent(
			String serviceId,
			String csInstanceId,
			String epsId,
			String eventId,
			String optionalInput)
			throws ClassNotFoundException, IOException, JAXBException {

		if (StringUtils.isBlank(eventId)) {
			return;
		}

		CustomEvent event = new CustomEvent(serviceId, csInstanceId, serviceId, eventId, USER_ID, epsId,
				optionalInput);
		lcManager.executeAction(event);
	}

	/**
	 * Only for Testing !
	 * 
	 * @param name
	 */
	public void insertRunningService(String name) {
		try {

			log.info("TEST inserting from SALSA '{}'", name);

			String instanceId = name;
			String serviceId = instanceId + "_FROM_SALSA";

			CloudService service = deployment.getService(instanceId);

			service.setName(serviceId);
			service.setId(serviceId);
			infoService.createService(service);
			infoService.createServiceInstance(service.getId(), instanceId);
			infoService.assignSupportingService(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

			service = infoService.getServiceInstance(instanceId);
			service.setName(instanceId);
			service.setId(instanceId);
			service = deployment.refreshStatus(service);

			for (ServiceUnit unit : Navigator.getAllUnits(service)) {
				for (UnitInstance instance : unit.getInstances()) {
					infoService.addUnitInstance(serviceId, instanceId, unit.getId(), instance);
				}
			}

			service.setName(serviceId);
			service.setId(serviceId);
			lcManager.hardSetRunning(service, instanceId);

		} catch (CoreServiceException | ComotException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

}
