package at.ac.tuwien.dsg.comot.m.adapter.example;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Bindings;
import at.ac.tuwien.dsg.comot.m.adapter.general.IManager;
import at.ac.tuwien.dsg.comot.m.adapter.general.IProcessor;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class SampleProcessor implements IProcessor {

	@Autowired
	protected InfoClient infoService; // may access information service

	protected IManager manager;
	protected String participantId;

	@Override
	public void init(IManager manager, String participantId) {
		this.manager = manager;
		this.participantId = participantId;
	}

	@Override
	public Bindings getBindings(String serviceId) {

		return new Bindings()
				// event that signals that the cloud service was deployed at this moment
				.addLifecycle(
						serviceId + "." + Action.DEPLOYED + ".*.TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#")
				// event that signals that the cloud service was stopped
				.addLifecycle(serviceId + "." + Action.STOP + "." + Type.SERVICE + ".#")
				// every Custom event concerning the cloud service
				.addCustom(serviceId + ".#");
	}

	@Override
	public void onLifecycleEvent(String serviceId, String groupId, Action action, CloudService service,
			Map<String, Transition> transitions, LifeCycleEvent event) throws Exception {

		// filter and process events
		if (action == Action.DEPLOYED) {
			// processing 1

			// send a custom event
			manager.sendCustomEvent(serviceId, groupId, SampleStandalone.SOME_CUSTOM_EVENT, null, null);

		} else if (action == Action.STOP) {
			// processing 2
		}
	}

	@Override
	public void onCustomEvent(String serviceId, String groupId, String eventName, String epsId, String optionalMessage,
			Map<String, Transition> transitions, CustomEvent event) throws Exception {

		// filter and process events
		if (eventName.equals(SampleStandalone.SOME_CUSTOM_EVENT)) {
			// processing 3
		} else {
			// processing 4
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg) throws Exception {
		// not used
	}

}
