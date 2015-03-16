package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.ComotAction;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
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
public class MonitoringAdapter extends AdapterCore {

	@Autowired
	protected MonitoringClient monitoring;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

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
		monitoring.setHostAndPort(ip, new Integer(port));
	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.UPDATE_FINISHED + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_FINISHED + ".#"));
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

		// when current RUNNING previous DEPLOYING and change TRUE
		// if monitored -> update
		// if not -> start
		if (action == Action.DEPLOYED) {

			CloudService servicefromInfo = infoService.getServiceInstance(serviceId, instanceId);
			servicefromInfo.setId(instanceId);
			servicefromInfo.setName(instanceId);

			if (monitoring.isMonitored(instanceId)) {
				monitoring.updateService(instanceId, servicefromInfo);
			} else {
				monitoring.startMonitoring(servicefromInfo);
			}

			// when action UPDATE_FINISHED or EL_CHANGE_FINISHED -> update
		} else if (action == Action.ELASTIC_CHANGE_FINISHED || action == Action.UPDATE_FINISHED) {

			CloudService servicefromInfo = infoService.getServiceInstance(serviceId, instanceId);
			servicefromInfo.setId(instanceId);
			servicefromInfo.setName(instanceId);

			monitoring.updateService(instanceId, servicefromInfo);

			// when current FINAL and change TRUE -> stop
		} else if (action == Action.UNDEPLOYED) {
			if (monitoring.isMonitored(instanceId)) {
				monitoring.stopMonitoring(instanceId);
			}
		}

	}

	@Override
	protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String optionalMessage) throws Exception {

		State stateService = msg.getTransitions().get(serviceId).getCurrentState();

		if (EpsAction.EPS_ASSIGNED.toString().equals(event)) {
			startIfActive(instanceId, stateService);

		} else if (EpsAction.EPS_ASSIGNMENT_REMOVED.toString().equals(event)) {
			if (monitoring.isMonitored(instanceId)) {
				monitoring.stopMonitoring(instanceId);
			}

		} else if (event.equals(ComotAction.MELA_START.toString())) {
			startIfActive(instanceId, stateService);

		} else if (event.equals(ComotAction.MELA_STOP.toString())) {
			if (monitoring.isMonitored(instanceId)) {
				monitoring.stopMonitoring(instanceId);
			}

		} else if (event.equals(ComotAction.MELA_SET_MCR.toString())) {

		} else if (event.equals(ComotAction.MELA_GET_MCR.toString())) {

		} else if (event.equals(ComotAction.MELA_GET_STRUCTURE.toString())) {

		}

	}

	protected void atStartUpServeWhatIsWaiting() {

		Map<String, String> instances = infoService.getInstancesHavingThisOsuAssigned(adapterId);

		for (String instanceId : instances.keySet()) {
			try {

				if (!monitoring.isMonitored(instanceId)) {
					State state = lcManager.getCurrentStateService(instanceId);
					startIfActive(instanceId, state);
				}

			} catch (ClassNotFoundException | IOException | EpsException | ComotException e) {
				e.printStackTrace();

			}
		}
	}

	protected void startIfActive(String instanceId, State state) throws EpsException, ComotException,
			ClassNotFoundException, IOException {

		CloudService servicefromInfo = infoService.getServiceInstance(instanceId);

		servicefromInfo.setId(instanceId);
		servicefromInfo.setName(instanceId);

		if (state == State.RUNNING || state == State.ELASTIC_CHANGE || state == State.UPDATE) {
			monitoring.startMonitoring(servicefromInfo);
		}
	}

}
