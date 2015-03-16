package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

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

import at.ac.tuwien.dsg.comot.m.common.ComotAction;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general.AdapterCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class ControlAdapter extends AdapterCore {

	@Autowired
	protected ControlClient control;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());
	protected Set<String> controlledSet = Collections.synchronizedSet(new HashSet<String>());

	@Override
	public void start(String osuInstanceId) {

		OfferedServiceUnit osu = infoService.getOsus().get(osuInstanceId);
		String ip = null;
		String port = null;

		for (Resource res : osu.getResources()) {
			if (res.getType().getName().equals(InformationServiceMock.IP)) {
				ip = res.getName();
			} else if (res.getType().getName().equals(InformationServiceMock.PORT)) {
				port = res.getName();
			}
		}
		control.setHostAndPort(ip, new Integer(port));

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
				instanceId + "." + adapterId + ".*.SERVICE"));

		return bindings;
	}

	@Override
	protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws Exception {

		if (action == Action.DEPLOYED) {
			control(instanceId);
		} else if (action == Action.UNDEPLOYED) {
			removeManaged(instanceId);
		} else if (action == Action.UPDATE_STARTED) {
			stopControl(instanceId);
		}

		log.info("FINISHED {}", action);
	}

	@Override
	protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String optionalMessage) throws Exception {

		State stateService = msg.getTransitions().get(serviceId).getCurrentState();

		if (EpsAction.EPS_ASSIGNED.toString().equals(event)) {
			if (stateService == State.RUNNING) {
				control(instanceId);
			}

		} else if (EpsAction.EPS_ASSIGNMENT_REMOVED.toString().equals(event)) {

			removeManaged(instanceId);

		} else if (event.equals(ComotAction.RSYBL_START.toString())) {

			control(instanceId);

		} else if (event.equals(ComotAction.RSYBL_STOP.toString())) {

			stopControl(instanceId);

		} else if (event.equals(ComotAction.RSYBL_SET_MCR.toString())) {

		} else if (event.equals(ComotAction.RSYBL_SET_EFFECTS.toString())) {

		}

		log.info("FINISHED {}", event);

	}

	protected void atStartUpServeWhatIsWaiting() {

		Map<String, String> instances = infoService.getInstancesHavingThisOsuAssigned(adapterId);

		for (String instanceId : instances.keySet()) {
			try {

				State state = lcManager.getCurrentStateService(instanceId);

				if (state == State.RUNNING) {
					control(instanceId);
				}

			} catch (ClassNotFoundException | IOException | EpsException | JAXBException e) {
				e.printStackTrace();

			}
		}
	}

	protected void manage(String instanceId) throws ClassNotFoundException, IOException, EpsException, JAXBException {

		if (!isManaged(instanceId)) {

			CloudService servicefromInfo = infoService.getServiceInstance(instanceId);

			servicefromInfo.setId(instanceId);
			servicefromInfo.setName(instanceId);

			control.sendInitialConfig(servicefromInfo);

			managedSet.add(instanceId);
		}
	}

	protected void control(String instanceId) throws EpsException, ClassNotFoundException, IOException, JAXBException {

		manage(instanceId);

		if (!isControlled(instanceId)) {
			control.startControl(instanceId);
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

}
