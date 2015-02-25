/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class Coordinator {

	private static final Logger log = LoggerFactory.getLogger(Coordinator.class);

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
		log.info("instanceId {}", instanceId);

		CloudService service = infoServ.getServiceInstance(instanceId);

		EventMessage event = new EventMessage(serviceId, instanceId, serviceId, Action.NEW_INSTANCE_REQUESTED, service,
				null);
		lcManager.executeAction(event);

		return instanceId;
	}

	public void assignSupportingOsu(String csInstanceId, String osuInstanceId) {
		infoServ.assignSupportingService(csInstanceId, osuInstanceId);
	}

	public void triggerCustomEvent(
			String serviceId,
			String csInstanceId,
			String osuInstanceId,
			String eventId,
			String optionalInput)
			throws ClassNotFoundException, IOException, JAXBException {

		// TODO check that the eps is assigned and the event exist

		EventMessage event = new EventMessage(serviceId, csInstanceId, serviceId, osuInstanceId + eventId,
				optionalInput);
		lcManager.executeAction(event);
	}

	public List<CloudService> getServices() {

		List<CloudService> list = new ArrayList<CloudService>(infoServ.getServices().values());
		// Collections.sort(list, comparator);

		return list;
	}

	public CloudService getServiceInstance(String instanceId) throws CoreServiceException, ComotException,
			ClassNotFoundException, IOException {

		CloudService service = infoServ.getServiceInstance(instanceId);

		return service;
	}

	// public ElementMonitoring getMonitoringData(String serviceId) throws CoreServiceException, ComotException {
	//
	// return monitoring.getMonitoringData(serviceId);
	// }

}
