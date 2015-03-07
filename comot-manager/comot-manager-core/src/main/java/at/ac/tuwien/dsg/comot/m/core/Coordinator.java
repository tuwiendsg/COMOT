/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.EpsAction;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class Coordinator {

	private static final Logger log = LoggerFactory.getLogger(Coordinator.class);

	public static final String USER_ID = "Some User";

	@Autowired
	protected InformationServiceMock infoServ;
	@Autowired
	protected LifeCycleManager lcManager;

	public String createCloudService(CloudService service) {

		String serviceId = infoServ.createService(service);
		return serviceId;
	}

	public String createServiceInstance(String serviceId) throws IOException, JAXBException, ClassNotFoundException {

		String instanceId = infoServ.createServiceInstance(serviceId);

		EventMessage event = new EventMessage(serviceId, instanceId, serviceId, Action.INSTANCE_CREATED,
				USER_ID, null, null);
		lcManager.executeAction(event);

		return instanceId;
	}

	public void startServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException,
			ClassNotFoundException {

		EventMessage event = new EventMessage(serviceId, instanceId, serviceId, Action.STARTED, USER_ID, null, null);
		lcManager.executeAction(event);

	}

	public void stopServiceInstance(String serviceId, String instanceId)
			throws IOException, JAXBException, ClassNotFoundException {

		EventMessage event = new EventMessage(serviceId, instanceId, serviceId, Action.STOPPED, USER_ID, null,
				null);
		lcManager.executeAction(event);

	}

	public void assignSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		infoServ.assignSupportingService(serviceId, instanceId, osuInstanceId);

		EventMessage event = new EventMessage(serviceId, instanceId, serviceId, EpsAction.EPS_ASSIGNED.toString(),
				USER_ID, osuInstanceId);
		lcManager.executeAction(event);

	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		infoServ.removeAssignmentOfSupportingOsu(serviceId, instanceId, osuInstanceId);

		EventMessage event = new EventMessage(serviceId, instanceId, serviceId,
				EpsAction.EPS_ASSIGNMENT_REMOVED.toString(), USER_ID, osuInstanceId);
		lcManager.executeAction(event);

	}

	public void triggerCustomEvent(
			String serviceId,
			String csInstanceId,
			String osuInstanceId,
			String eventId,
			String optionalInput)
			throws ClassNotFoundException, IOException, JAXBException {

		// TODO check that the eps is assigned and the event exist

		EventMessage event = new EventMessage(serviceId, csInstanceId, serviceId, eventId, USER_ID,
				optionalInput);
		lcManager.executeAction(event);
	}

}
