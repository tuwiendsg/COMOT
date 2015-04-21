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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;

@Component
public class Coordinator {

	private static final Logger log = LoggerFactory.getLogger(Coordinator.class);

	public static final String USER_ID = "Some_User";

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

	public String createCloudService(CloudService service) throws EpsException {

		String serviceId = infoService.createService(service);
		return serviceId;
	}

	public String createServiceInstance(String serviceId) throws EpsException, AmqpException, JAXBException {

		String instanceId = infoService.createServiceInstance(serviceId);

		log.info("sending CREATE");
		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.CREATED));

		return instanceId;
	}

	public void startServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException {

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.START));

	}

	public void stopServiceInstance(String serviceId, String instanceId) throws IOException, JAXBException {

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STOP));

	}

	public void removeServiceInstance(String serviceId, String instanceId) throws AmqpException, JAXBException {

		// infoService.removeServiceInstance(serviceId, instanceId);

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.REMOVED));

	}

	public void reconfigureElasticity(String serviceId, String instanceId, CloudService service) throws AmqpException,
			JAXBException {

		sendLifeCycle(Type.SERVICE, new LifeCycleEventModifying(serviceId, instanceId, serviceId,
				Action.RECONFIGURE_ELASTICITY, null, service));
	}

	public void kill(String serviceId, String instanceId) throws AmqpException, JAXBException {

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.KILL));
	}

	public void assignSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws IOException, JAXBException {

		// infoService.assignSupportingService(serviceId, instanceId, osuInstanceId);

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, instanceId, serviceId, EpsEvent.EPS_SUPPORT_REQUESTED.toString(),
						osuInstanceId, null));

	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String instanceId, String osuInstanceId)
			throws ClassNotFoundException, IOException, JAXBException {

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, instanceId, serviceId, EpsEvent.EPS_SUPPORT_REMOVED.toString(),
						osuInstanceId, null));

	}

	public String createDynamicService(String epsId) throws AmqpException, JAXBException, EpsException {

		return createServiceInstance(infoService.getOsu(epsId).getService().getId());

		// CustomEvent event = new CustomEvent(null, null, null, EpsAction.EPS_DYNAMIC_REQUESTED.toString(),
		// Constants.EPS_BUILDER, epsId);
		// StateMessage msg = new StateMessage(event, null, null);
		// amqp.convertAndSend(Constants.EXCHANGE_DYNAMIC_REGISTRATION, epsId, Utils.asJsonString(msg));

	}

	public void removeDynamicService(String epsId, String epsInstanceId) throws AmqpException, JAXBException,
			EpsException {

		OsuInstance osuInatance = infoService.getOsuInstance(epsInstanceId);

		String serviceId = osuInatance.getOsu().getService().getId();
		String instanceId = osuInatance.getServiceInstance().getId();

		sendCustom(Type.SERVICE,
				new CustomEvent(serviceId, instanceId, serviceId, EpsEvent.EPS_DYNAMIC_REMOVED.toString(),
						Constants.EPS_BUILDER, null));

	}

	public void triggerCustomEvent(
			String serviceId,
			String csInstanceId,
			String epsId,
			String eventId,
			String optionalInput) throws AmqpException, JAXBException {

		if (StringUtils.isBlank(eventId)) {
			return;
		}

		sendCustom(Type.SERVICE, new CustomEvent(serviceId, csInstanceId, serviceId, eventId, epsId, optionalInput));

	}

	protected void sendLifeCycle(Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "." + event.getAction()
				+ "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		event.setOrigin(USER_ID);
		event.setTime(System.currentTimeMillis());

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	protected void sendCustom(Type targetLevel, CustomEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "."
				+ event.getCustomEvent() + "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);
		event.setOrigin(USER_ID);
		event.setTime(System.currentTimeMillis());

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}
}
