/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class Coordinator {

	private static final Logger log = LoggerFactory.getLogger(Coordinator.class);

	public static final String USER_ID = "Some_User";

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected RabbitTemplate amqp;

	public String createCloudService(CloudService service) throws ClassNotFoundException, IOException {

		String serviceId = infoService.createService(service);
		return serviceId;
	}

	public String createServiceInstance(String serviceId) throws IOException, JAXBException, ClassNotFoundException {

		String instanceId = infoService.createServiceInstance(serviceId);

		log.info("sending CREATE");
		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.CREATED, USER_ID, null));

		return instanceId;
	}

	public void startServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException,
			ClassNotFoundException {

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STARTED, USER_ID, null));

	}

	public void stopServiceInstance(String serviceId, String instanceId)
			throws IOException, JAXBException, ClassNotFoundException {

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STOPPED, USER_ID, null));

	}

	public void removeServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException,
			ClassNotFoundException {

		infoService.removeServiceInstance(serviceId, instanceId);

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.REMOVED, USER_ID, null));

	}

	public void assignSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		// infoService.assignSupportingService(serviceId, instanceId, osuInstanceId);

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, instanceId, serviceId, EpsAction.EPS_ASSIGNMENT_REQUESTED.toString(),
						USER_ID, osuInstanceId, null));

	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, instanceId, serviceId, EpsAction.EPS_ASSIGNMENT_REMOVED.toString(), USER_ID,
						osuInstanceId, null));

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

		sendCustom(Type.SERVICE, new CustomEvent(serviceId, csInstanceId, serviceId, eventId, USER_ID, epsId,
				optionalInput));

	}

	protected void sendLifeCycle(Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "." + event.getAction()
				+ "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		amqp.convertAndSend(AppContextCore.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	protected void sendCustom(Type targetLevel, CustomEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "."
				+ event.getCustomEvent() + "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		amqp.convertAndSend(AppContextCore.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
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

			if (deployment.isManaged(instanceId)) {

				CloudService service = deployment.getService(instanceId);

				service.setName(serviceId);
				service.setId(serviceId);

				createCloudService(service);

				infoService.createServiceInstance(serviceId, instanceId);
				sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.CREATED,
						USER_ID, null));

				assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

				service = infoService.getServiceInstance(instanceId);
				service.setName(instanceId);
				service.setId(instanceId);
				service = deployment.refreshStatus(service);

				Thread.sleep(3000);

				service.setName(serviceId);
				service.setId(serviceId);
				lcManager.hardSetRunning(service, instanceId);
			}

		} catch (EpsException | ComotException | ClassNotFoundException | IOException | JAXBException
				| InterruptedException e) {
			e.printStackTrace();
		}
	}

}
