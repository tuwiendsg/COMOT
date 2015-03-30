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
import at.ac.tuwien.dsg.comot.m.common.ComotAction;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlEventsListener;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
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
				instanceId + ".*.*.*." + Action.UPDATE_STARTED + ".#"));
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
		} else if (action == Action.UPDATE_STARTED) {
			stopControl(instanceId);
		}

	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		State stateService = msg.getTransitions().get(serviceId).getCurrentState();

		if (EpsAction.EPS_SUPPORT_ASSIGNED.toString().equals(event)) {
			if (stateService == State.RUNNING) {
				control(serviceId, instanceId);
			}

		} else if (EpsAction.EPS_SUPPORT_REMOVED.toString().equals(event)) {

			removeManaged(instanceId);

		} else if (event.equals(ComotAction.RSYBL_START.toString())) {

			control(serviceId, instanceId);

		} else if (event.equals(ComotAction.RSYBL_STOP.toString())) {

			stopControl(instanceId);

		} else if (event.equals(ComotAction.RSYBL_SET_MCR.toString())) {

		} else if (event.equals(ComotAction.RSYBL_SET_EFFECTS.toString())) {

		}

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId, Exception e)
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

		try {

			String instanceId = event.getServiceId();

			if (event instanceof ActionPlanEvent) {
				ActionPlanEvent apEvent = (ActionPlanEvent) event;

				// log.info("onActionPlanEvent(serviceId={}, stage={}, type={}, strategies={}, constraints={}, effects={})",
				// apEvent.getServiceId(), apEvent.getStage(), apEvent.getType(), apEvent.getStrategies(),
				// apEvent.getConstraints(), apEvent.getEffect());

			} else if (event instanceof ActionEvent) {
				ActionEvent aEvent = (ActionEvent) event;

				// log.info("onActionEvent(serviceId={}, stage={}, type={}, actionId={}, targetId={})",
				// aEvent.getServiceId(),
				// aEvent.getStage(), aEvent.getType(), aEvent.getActionId(), aEvent.getTargetId());

				CloudService instance = infoService.getServiceInstance(instanceId);
				String serviceId = instance.getId();
				String groupId = aEvent.getTargetId();

				Navigator nav = new Navigator(instance);
				Type type = Type.decide(nav.getManaged(groupId));

				if (event.getStage() == IEvent.Stage.START) {
					manager.sendLifeCycle(type, new LifeCycleEvent(serviceId, instanceId, groupId,
							Action.ELASTIC_CHANGE_STARTED));

				} else if (event.getStage() == IEvent.Stage.FINISHED) {
					manager.sendLifeCycle(type, new LifeCycleEvent(serviceId, instanceId, groupId,
							Action.ELASTIC_CHANGE_FINISHED));
				}

			}
		} catch (Exception e) {

		}
	}

}
