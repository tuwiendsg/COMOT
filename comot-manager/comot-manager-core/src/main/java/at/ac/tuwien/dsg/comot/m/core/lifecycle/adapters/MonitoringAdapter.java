package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class MonitoringAdapter extends Adapter {

	@Autowired
	protected MonitoringClient monitoring;

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

		bindingLifeCycle("*.TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#");
		bindingLifeCycle("*.*.*.*." + Action.UPDATE_FINISHED + ".#");
		bindingLifeCycle("*.*.*.*." + Action.ELASTIC_CHANGE_FINISHED + ".#");
		bindingLifeCycle("*.TRUE.*." + State.PASSIVE + ".#");

		bindingCustom("*." + adapterId + ".*.SERVICE");

		container.setMessageListener(new CustomListener(osuInstanceId));

		atStartUpServeWhatIsWaiting();

	}

	class CustomListener extends AdapterListener {

		public CustomListener(String adapterId) {
			super(adapterId);
		}

		@Override
		protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
				throws ClassNotFoundException, IOException, CoreServiceException, ComotException {

			if (infoService.isOsuAssignedToInstance(serviceId, instanceId, adapterId)) {

				// when current RUNNING previous DEPLOYING and change TRUE
				// if monitored -> update
				// if not -> start
				if (action == Action.DEPLOYED) {

					CloudService servicefromInfo = infoService.getServiceInstance(serviceId, instanceId);
					servicefromInfo.setId(instanceId);
					servicefromInfo.setName(instanceId);

					if (isMonitored(instanceId)) {
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
					if (isMonitored(instanceId)) {
						monitoring.stopMonitoring(instanceId);
					}
				}
			}
		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String epsId, String optionalMessage) throws ClassNotFoundException,
				CoreServiceException, ComotException, IOException {

			if (adapterId.equals(epsId)) {

				State stateService = msg.getTransitions().get(serviceId).getCurrentState();

				if (EpsAction.EPS_ASSIGNED.toString().equals(event)) {
					startIfActive(instanceId, stateService);

				} else if (EpsAction.EPS_ASSIGNMENT_REMOVED.toString().equals(event)) {
					if (isMonitored(instanceId)) {
						monitoring.stopMonitoring(instanceId);
					}

				} else if (event.equals(ComotAction.MELA_START.toString())) {
					startIfActive(instanceId, stateService);

				} else if (event.equals(ComotAction.MELA_STOP.toString())) {
					if (isMonitored(instanceId)) {
						monitoring.stopMonitoring(instanceId);
					}

				} else if (event.equals(ComotAction.MELA_SET_MCR.toString())) {

				} else if (event.equals(ComotAction.MELA_GET_MCR.toString())) {

				} else if (event.equals(ComotAction.MELA_GET_STRUCTURE.toString())) {

				}
			}
		}

	}

	protected void atStartUpServeWhatIsWaiting() {

		Map<String, String> instances = infoService.getInstancesHavingThisOsuAssigned(adapterId);

		for (String instanceId : instances.keySet()) {
			try {

				if (!isMonitored(instanceId)) {
					State state = lcManager.getCurrentStateService(instanceId);
					startIfActive(instanceId, state);
				}

			} catch (ClassNotFoundException | IOException | CoreServiceException | ComotException e) {
				e.printStackTrace();

			}
		}
	}

	protected void startIfActive(String instanceId, State state) throws CoreServiceException, ComotException,
			ClassNotFoundException, IOException {

		CloudService servicefromInfo = infoService.getServiceInstance(instanceId);

		servicefromInfo.setId(instanceId);
		servicefromInfo.setName(instanceId);

		if (state == State.RUNNING || state == State.ELASTIC_CHANGE || state == State.UPDATE) {
			monitoring.startMonitoring(servicefromInfo);
		}
	}

	protected boolean isMonitored(String instanceId) throws CoreServiceException {

		for (String id : monitoring.listAllServices()) {
			if (id.equals(instanceId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void clean() {
	}

}
