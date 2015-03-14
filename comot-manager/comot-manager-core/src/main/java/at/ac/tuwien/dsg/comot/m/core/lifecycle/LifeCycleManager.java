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

import at.ac.tuwien.dsg.comot.m.common.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.RecordingAdapter;
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

		RecordingAdapter recording = context.getBean(RecordingAdapter.class);
		recording.startAdapter(InformationServiceMock.RECORDER);

	}

	public void executeAction(AbstractEvent event) throws IOException,
			JAXBException, ClassNotFoundException {

		log.info("EXECUTE ACTION: ( {})", event);

		ManagerOfServiceInstance manager;
		CloudService service;
		String csInstanceId = event.getCsInstanceId();
		String serviceId = event.getServiceId();

		if (event instanceof LifeCycleEvent) {
			LifeCycleEvent eventLc = (LifeCycleEvent) event;

			if (Action.REMOVED != eventLc.getAction()) {
				// clean service
				if (eventLc.getService() == null) {
					service = infoService.getServiceInstance(serviceId, csInstanceId);
				} else {
					service = (CloudService) Utils.deepCopy(eventLc.getService());
				}
				eventLc.setService(UtilsLc.removeProviderInfo(service));
			}

			if (Action.CREATED == eventLc.getAction()) {

				if (managers.containsKey(csInstanceId)) {
					return;
				}

				manager = context.getBean(ManagerOfServiceInstance.class);
				managers.put(csInstanceId, manager);

				checkAndExecute(csInstanceId, event);

			} else if (Action.REMOVED == eventLc.getAction()) {

				checkAndExecute(csInstanceId, event);

				managers.remove(managers.get(csInstanceId));

			} else {
				checkAndExecute(csInstanceId, event);
			}

		} else {
			checkAndExecute(csInstanceId, event);
		}

	}

	protected void checkAndExecute(String csInstanceId, AbstractEvent event) throws ClassNotFoundException,
			JAXBException, IOException {

		if (!managers.containsKey(csInstanceId)) {
			throw new ComotIllegalArgumentException("Instance '" + csInstanceId + "' has no managed life-cycle");
		}

		managers.get(csInstanceId).executeActionAny(event);
	}

	public State getCurrentState(String instanceId, String groupId) {
		return managers.get(instanceId).getCurrentState(groupId);
	}

	public State getCurrentStateService(String instanceId) {
		return managers.get(instanceId).getCurrentStateService();

	}

	public Map<String, Transition> getCurrentState(String instanceId) {
		return managers.get(instanceId).getCurrentState();
	}

	public boolean isInstanceManaged(String instanceId) {
		if (managers.containsKey(instanceId)) {
			return true;
		} else {
			return false;
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

	/**
	 * Only for testing!
	 * 
	 * @param instanceId
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void hardSetRunning(CloudService service, String instanceId) throws ClassNotFoundException, IOException {

		ManagerOfServiceInstance manager = context.getBean(ManagerOfServiceInstance.class);
		managers.put(instanceId, manager);

		manager.hardSetRunning(instanceId, service);
	}
}
