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
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected InformationServiceMock infoService;

	protected Map<String, ManagerOfServiceInstance> managers = new HashMap<>();

	public LifeCycleManager() {
	}

	@PostConstruct
	public void setUp() {

		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_LIFE_CYCLE, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_CUSTOM_EVENT, false, false));
	}

	public State getCurrentState(String instanceId, String groupId) {
		return managers.get(instanceId).getCurrentState(groupId);

	}

	public Map<String, Transition> getCurrentState(String instanceId) {
		return managers.get(instanceId).getCurrentState();
	}

	public void executeAction(EventMessage event) throws IOException,
			JAXBException, ClassNotFoundException {

		log.info("EXECUTE ACTION: ( {})", event);

		ManagerOfServiceInstance manager;
		CloudService service = event.getService();
		String csInstanceId = event.getCsInstanceId();
		String serviceId = event.getServiceId();

		// clean service
		if (service == null) {
			service = infoService.getServiceInstance(serviceId, csInstanceId);
		}
		event.setService(UtilsLc.removeProviderInfo(service));

		if (Action.INSTANCE_CREATED.equals(event.getAction())) {

			if (managers.containsKey(csInstanceId)) {
				return;
			}

			manager = context.getBean(ManagerOfServiceInstance.class);
			managers.put(csInstanceId, manager);

		} else if (Action.INSTANCE_REMOVED.equals(event.getAction())) {

			// TODO delete manager, remove exchanges

		}

		if (!managers.containsKey(csInstanceId)) {
			throw new ComotIllegalArgumentException("Instance '" + csInstanceId + "' has no managed life-cycle");
		}

		if (event.isLifeCycleDefined()) {
			managers.get(csInstanceId).executeAction(event);

		} else {
			managers.get(csInstanceId).executeCustomAction(event);
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
