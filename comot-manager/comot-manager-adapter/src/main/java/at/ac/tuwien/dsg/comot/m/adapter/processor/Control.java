package at.ac.tuwien.dsg.comot.m.adapter.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.ComotEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlEventsListener;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionPlanEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;

@Component
@Scope("prototype")
public class Control extends Processor implements ControlEventsListener {

	@Autowired
	protected ControlClient control;
	@Autowired
	protected InformationClient infoService;

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());
	protected Set<String> controlledSet = Collections.synchronizedSet(new HashSet<String>());

	public void setHostAndPort(String host, int port) {
		control.setHostAndPort(host, port);
	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.MAINTENANCE_STARTED + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*." + State.PASSIVE + ".#"));

		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + ".*.SERVICE"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws Exception {

		if (action == Action.DEPLOYED) {
			control(serviceId, instanceId);
		} else if (action == Action.UNDEPLOYED) {
			removeManaged(instanceId);
		} else if (action == Action.MAINTENANCE_STARTED) {
			stopControl(instanceId);
		}

	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		State stateService = msg.getTransitions().get(serviceId).getCurrentState();

		if (EpsEvent.EPS_SUPPORT_ASSIGNED.toString().equals(event)) {
			if (stateService == State.RUNNING) {
				control(serviceId, instanceId);
			}

		} else if (EpsEvent.EPS_SUPPORT_REMOVED.toString().equals(event)) {

			removeManaged(instanceId);

		} else if (event.equals(ComotEvent.RSYBL_START.toString())) {
			if (stateService == State.RUNNING) {
				control(serviceId, instanceId);
			}
		} else if (event.equals(ComotEvent.RSYBL_STOP.toString())) {

			stopControl(instanceId);

		} else if (event.equals(ComotEvent.RSYBL_SET_MCR.toString())) {

		} else if (event.equals(ComotEvent.RSYBL_SET_EFFECTS.toString())) {

		}

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId)
			throws Exception {
		// TODO Auto-generated method stub

	}

	protected void manage(String serviceId, String instanceId) throws ClassNotFoundException, IOException,
			EpsException, JAXBException {

		if (!isManaged(instanceId)) {

			CloudService servicefromInfo = infoService.getServiceInstance(instanceId);

			servicefromInfo.setId(instanceId);
			servicefromInfo.setName(instanceId);

			control.sendInitialConfig(servicefromInfo);

			managedSet.add(instanceId);
		}
	}

	protected void control(String serviceId, String instanceId) throws Exception {

		manage(serviceId, instanceId);

		if (!isControlled(instanceId)) {
			control.startControl(instanceId);
			control.registerForEvents(instanceId, this);
			controlledSet.add(instanceId);
		}
	}

	protected void removeManaged(String instanceId) throws EpsException {

		stopControl(instanceId);

		if (isManaged(instanceId)) {
			control.removeService(instanceId);
			managedSet.remove(instanceId);
		}

	}

	protected void stopControl(String instanceId) throws EpsException {
		if (isControlled(instanceId)) {
			control.stopControl(instanceId);
			controlledSet.remove(instanceId);
		}
	}

	protected boolean isManaged(String instanceId) {
		return managedSet.contains(instanceId);
	}

	protected boolean isControlled(String instanceId) {
		return controlledSet.contains(instanceId);
	}

	@Override
	public void onMessage(IEvent event) {

		String instanceId = null;
		String serviceId = null;
		try {
			String optionalMsg = null;
			instanceId = event.getServiceId();
			CloudService instance = infoService.getServiceInstance(instanceId);
			serviceId = instance.getId();
			String customEventName = ComotEvent.rsyblEventName(event);

			try {
				optionalMsg = Utils.asJsonString(event);
			} catch (Exception e) {
				log.error("Failed to marshall message: {}", event.getClass());
				log.error("{}", e);
				throw e;
			}

			if (event instanceof ActionPlanEvent) {
				ActionPlanEvent apEvent = (ActionPlanEvent) event;

				Type type = Type.SERVICE;

				if (apEvent.getStage() == IEvent.Stage.START) {
					manager.sendLifeCycle(type, new LifeCycleEvent(serviceId, instanceId, serviceId,
							Action.ELASTIC_CHANGE_STARTED));

					manager.sendCustom(Type.SERVICE, new CustomEvent(serviceId, instanceId, serviceId,
							customEventName, null, optionalMsg));

				} else if (apEvent.getStage() == IEvent.Stage.FINISHED || apEvent.getStage() == IEvent.Stage.FINISHED) {

					manager.sendCustom(Type.SERVICE, new CustomEvent(serviceId, instanceId, serviceId,
							customEventName, null, optionalMsg));

					manager.sendLifeCycle(type, new LifeCycleEvent(serviceId, instanceId, serviceId,
							Action.ELASTIC_CHANGE_FINISHED));
				}

			} else {
				String targetId;
				if (event instanceof ActionEvent) {
					ActionEvent aEvent = (ActionEvent) event;

					targetId = aEvent.getTargetId();

				} else if (event instanceof at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent) {
					at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent cEvent = (at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent) event;
					targetId = serviceId;
				} else {
					targetId = serviceId;
				}

				manager.sendCustom(Type.SERVICE, new CustomEvent(serviceId, instanceId, targetId,
						customEventName, null, optionalMsg));
			}

			log.info("sending custom event with optional message: {}", optionalMsg);

		} catch (Exception e) {
			try {
				manager.sendException(serviceId, instanceId, e);
			} catch (Throwable e1) {
				log.error("{}", e1);
			}
			log.error("{}", e);
		}
	}
}

// CloudService instance = infoService.getServiceInstance(instanceId);
// String serviceId = instance.getId();
// String targetGroupId = aEvent.getTargetId();
// Set<String> allTargetIds = new HashSet<>();
// allTargetIds.add(targetGroupId);
//
// Navigator nav = new Navigator(instance);
// Type type = Type.decide(nav.getManaged(targetGroupId));
//
// // solve mismatch between rSYBL units and information model units
// if (type == Type.UNIT) {
// for (ServiceUnit unit : nav.getHostsRecursive(nav.getUnit(targetGroupId))) {
// allTargetIds.add(unit.getId());
// }
// }
//
// for (String tempGroupId : allTargetIds) {
// if (event.getStage() == IEvent.Stage.START) {
// manager.sendLifeCycle(type, new LifeCycleEvent(serviceId, instanceId, tempGroupId,
// Action.ELASTIC_CHANGE_STARTED));
//
// } else if (event.getStage() == IEvent.Stage.FINISHED) {
// manager.sendLifeCycle(type, new LifeCycleEvent(serviceId, instanceId, tempGroupId,
// Action.ELASTIC_CHANGE_FINISHED));
// }
//
// }

