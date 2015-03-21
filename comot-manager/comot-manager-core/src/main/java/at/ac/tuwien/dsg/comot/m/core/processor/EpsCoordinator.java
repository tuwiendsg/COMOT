package at.ac.tuwien.dsg.comot.m.core.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.adapter.EpsAdapter;
import at.ac.tuwien.dsg.comot.m.core.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.core.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class EpsCoordinator extends Processor {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InformationServiceMock infoService;

	@Autowired
	protected AmqpAdmin admin;

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());

	@Override
	public void start() {

		for (OfferedServiceUnit osu : infoService.getOsus().values()) {
			try {
				createStaticEps(osu);
			} catch (BeansException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		// TODO bindings for dynamic eps created

	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {
		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingCustom(queueName, "*.*." + EpsAction.EPS_DYNAMIC_REQUESTED + ".SERVICE"));
		bindings.add(bindingCustom(queueName, "*.*." + EpsAction.EPS_DYNAMIC_REMOVED + ".SERVICE"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws ClassNotFoundException, IOException {

	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			String event, String epsId, String origin, String optionalMessage) throws ClassNotFoundException {

		EpsAction action = EpsAction.valueOf(event);
		State serviceState = msg.getTransitions().get(serviceId).getCurrentState();

		if (action == EpsAction.EPS_DYNAMIC_REQUESTED) {

			createDynamicEps(epsId);

		} else if (action == EpsAction.EPS_DYNAMIC_CREATED) {
			// TODO this replace by observation of lifecycle of the dynamic eps

			managedSet.add(epsId);

		} else if (action == EpsAction.EPS_DYNAMIC_REMOVED) {

			removeDynamicEps(epsId);

		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId,
			Exception e) throws Exception {
		// TODO Auto-generated method stub

	}

	protected void createStaticEps(OfferedServiceUnit osu) throws ClassNotFoundException {

		for (Resource res : osu.getResources()) {
			if (res.getType().getName().equals(InformationServiceMock.ADAPTER_CLASS)) {

				String epsId = osu.getId();
				Class<?> clazz = Class.forName(res.getName());

				EpsAdapter adapter = (EpsAdapter) context.getBean(clazz);

				admin.declareQueue(new Queue(PerInstanceQueueManager.queueNameAssignment(epsId), false, false,
						true));

				adapter.start(epsId);
				managedSet.add(epsId);

				break;
			}
		}

	}

	protected void createDynamicEps(String epsId) {

		// on EPS_INSTANTIATE_REQUEST

		// create the service

		// EPS creates its assignment queue on its own

		// EPS writes itself to the DB

		// coordinator listens on the lifecycle and notices when the eps is created

		throw new UnsupportedOperationException("createDynamicEps");
	}

	protected void removeDynamicEps(String epsId) {

		if (managedSet.contains(epsId)) {
			// TODO

			managedSet.remove(epsId);
		}
	}

}
