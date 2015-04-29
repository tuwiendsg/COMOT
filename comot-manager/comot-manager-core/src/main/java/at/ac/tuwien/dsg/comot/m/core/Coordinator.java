/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.adapter.general.SingleQueueManager;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.event.state.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;

@Component
public class Coordinator {

	private static final Logger log = LoggerFactory.getLogger(Coordinator.class);

	public static final String USER_ID = "Some_User";
	public static final long TIMEOUT = 30000;

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected RabbitTemplate amqp;

	@Autowired
	protected ToscaMapper mapperTosca;
	@javax.annotation.Resource
	public Environment env;

	public String createService(CloudService service) throws Exception {

		String serviceId = infoService.createService(service);
		log.info("serviceId {}", serviceId);
		sendLifeCycleWaitForId(new LifeCycleEventModifying(serviceId, serviceId, Action.CREATED, null, service));

		return serviceId;

	}

	public String createServiceFromTemplate(String templateId) throws Exception {

		String serviceId = infoService.createServiceFromTemplate(templateId);
		CloudService service = infoService.getService(serviceId);

		sendLifeCycleWaitForId(new LifeCycleEventModifying(serviceId, serviceId, Action.CREATED, null, service));

		return serviceId;
	}

	public void startService(String serviceId) throws Exception {

		sendLifeCycleWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.START));
	}

	public void stopService(String serviceId) throws Exception {

		sendLifeCycleWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.STOP));
	}

	public void removeService(String serviceId) throws Exception {

		sendLifeCycleWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.REMOVED));
	}

	public void reconfigureElasticity(String serviceId, CloudService service) throws Exception {

		sendLifeCycleWaitForId(new LifeCycleEventModifying(serviceId, serviceId,
				Action.RECONFIGURE_ELASTICITY, null, service));
	}

	public void kill(String serviceId) throws Exception {

		sendLifeCycleWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.KILL));
	}

	public void assignSupportingOsu(String serviceId, String osuInstanceId)
			throws IOException, JAXBException {

		log.info("coord assign {} {}", serviceId, osuInstanceId);

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, serviceId, EpsEvent.EPS_SUPPORT_REQUESTED.toString(), osuInstanceId, null));
	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, serviceId, EpsEvent.EPS_SUPPORT_REMOVED.toString(),
						osuInstanceId, null));
	}

	public String createDynamicService(String epsId) throws AmqpException, JAXBException, EpsException {

		String serviceId = infoService.createDynamicEpsInstance(epsId);

		sendCustom(Type.SERVICE,
				new CustomEvent(null, null, EpsEvent.EPS_DYNAMIC_REQUESTED.toString(), null, serviceId));

		log.info("coord {}", serviceId);

		return serviceId;
	}

	public void removeDynamicService(String epsId, String epsInstanceId) throws AmqpException, JAXBException,
			EpsException {

		OsuInstance osuInatance = infoService.getOsuInstance(epsInstanceId);

		String serviceId = osuInatance.getService().getId();

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, serviceId, EpsEvent.EPS_DYNAMIC_REMOVED.toString(),
						Constants.EPS_BUILDER, null));

	}

	public void triggerCustomEvent(
			String serviceId,
			String epsId,
			String eventId,
			String optionalInput) throws AmqpException, JAXBException {

		if (StringUtils.isBlank(eventId)) {
			return;
		}

		sendCustom(Type.SERVICE, new CustomEvent(serviceId, serviceId, eventId, epsId, optionalInput));

	}

	protected void sendLifeCycleWaitForId(final LifeCycleEvent event) throws Exception {

		final String evantId = event.getEventId();

		CoordinatorAdapter processor = new CoordinatorAdapter(event.getServiceId(), this) {
			@Override
			public void process(AbstractEvent event, boolean exception, ComotMessage msg) {
				if (event.getEventId().equals(evantId)) {

					response = msg;

					if (exception) {
						signal.result = false;
					} else {
						log.info("OK");
						signal.result = true;
					}
				}
			}

			@Override
			public void sendInternal() throws AmqpException, JAXBException {
				coordinator.sendLifeCycle(Type.SERVICE, event);
			}
		};

		Manager manager = context.getBean(SingleQueueManager.class);
		manager.start("C_" + UUID.randomUUID().toString(), processor);

		processor.send();

	}

	protected void sendLifeCycle(Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getServiceId() + "." + LifeCycleEvent.class.getSimpleName() + "." + event.getAction()
				+ "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		event.setOrigin(USER_ID);
		event.setTime(System.currentTimeMillis());

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	protected void sendCustom(Type targetLevel, CustomEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getServiceId() + "." + CustomEvent.class.getSimpleName() + "."
				+ event.getCustomEvent() + "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);
		event.setOrigin(USER_ID);
		event.setTime(System.currentTimeMillis());

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

}
