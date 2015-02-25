package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected AmqpAdmin admin;

	protected Map<String, ManagerOfServiceInstance> managers = new HashMap<>();

	public LifeCycleManager() {
	}

	@PostConstruct
	public void setUp() {

		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_INSTANCE_HIGH_LEVEL, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_INSTANCE_DETAILED, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_INSTANCE_CUSTOM, false, false));
	}

	public void executeAction(EventMessage event) throws IOException,
			JAXBException {

		log.info("executeAction( {})", event);

		String csInstanceId = event.getCsInstanceId();
		ManagerOfServiceInstance manager;

		if (Action.NEW_INSTANCE_REQUESTED.equals(event.getAction())) {

			if (managers.containsKey(csInstanceId)) {
				return;
			}

			manager = context.getBean(ManagerOfServiceInstance.class);
			managers.put(csInstanceId, manager);

			manager.executeAction(event);

		} else if (Action.INSTANCE_REMOVAL_REQUESTED.equals(event.getAction())) {

			// TODO delete manager, remove exchanges

		} else {

			log.info("managers {}", managers);

			if (!managers.containsKey(csInstanceId)) {
				throw new ComotIllegalArgumentException("Instance '" + csInstanceId + "' has no managed life-cycle");
			}

			if (event.isLifeCycleDefined()) {
				managers.get(csInstanceId).executeAction(event);

			} else {
				managers.get(csInstanceId).executeCustomAction(event);
			}
		}
	}

	// public void executeAction(EventMessage event) throws IOException, JAXBException {
	//
	// for (ManagerOfServiceInstance manager : managers.values()) {
	// if (manager.getServiceGroup().getId().equals(serviceId)) {
	// executeAction(serviceId, manager.getServiceGroup().getId(), groupId, action);
	// }
	// }
	//
	// }

}
