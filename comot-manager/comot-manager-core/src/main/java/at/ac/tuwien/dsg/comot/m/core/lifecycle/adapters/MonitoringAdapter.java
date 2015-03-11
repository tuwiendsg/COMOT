package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
// @Scope("prototype") //for some reason this creates multip0le instances
public class MonitoringAdapter extends Adapter {

	@Autowired
	protected MonitoringClient monitoring;

	protected Binding binding1;
	protected Binding binding2;
	protected Binding binding3;
	protected Binding binding4;

	@Override
	public void start(String osuInstanceId) {

		OfferedServiceUnit osu = infoService.getOsus().get(osuInstanceId);
		String ip = null;
		String port = null;

		for (Resource resource : osu.getResources()) {
			if (resource.getName().equals(InformationServiceMock.PUBLIC_INSTANCE)) {
				for (Resource res : resource.getContainsResources()) {
					if (res.getType().getName().equals(InformationServiceMock.IP)) {
						ip = res.getName();
					} else if (res.getType().getName().equals(InformationServiceMock.PORT)) {
						port = res.getName();
					}
				}
			}
		}

		monitoring.setHost(ip);
		monitoring.setPort(new Integer(port));

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#", null);
		binding2 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.*.*.*." + Action.UPDATE_FINISHED + ".#", null);
		binding3 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.*.*.*." + Action.ELASTIC_CHANGE_FINISHED + ".#", null);
		binding4 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.FINAL + ".#", null);

		admin.declareBinding(binding1);
		admin.declareBinding(binding2);
		admin.declareBinding(binding3);
		admin.declareBinding(binding4);

		container.setMessageListener(new CustomListener(osuInstanceId));

		atStartUpDeployWhatIsWaiting();

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
				} else if (action == Action.REMOVED) {
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

				if (EpsAction.EPS_ASSIGNED.toString().equals(event)) {
					startIfActive(instanceId);

				} else if (EpsAction.EPS_ASSIGNMENT_REMOVED.toString().equals(event)) {
					if (isMonitored(instanceId)) {
						monitoring.stopMonitoring(instanceId);
					}
				}
			}

			if (event.equals(ComotAction.MELA_START)) {
				startIfActive(instanceId);

			} else if (event.equals(ComotAction.MELA_STOP)) {
				if (isMonitored(instanceId)) {
					monitoring.stopMonitoring(instanceId);
				}

			} else if (event.equals(ComotAction.MELA_SET_MCR)) {

			} else if (event.equals(ComotAction.MELA_GET_MCR)) {

			} else if (event.equals(ComotAction.MELA_GET_STRUCTURE)) {

			}
		}

	}

	protected void atStartUpDeployWhatIsWaiting() {

		Map<String, String> instances = infoService.getInstancesHavingThisOsuAssigned(adapterId);

		for (String instanceId : instances.keySet()) {
			try {

				if (!isMonitored(instanceId)) {
					startIfActive(instanceId);
				}

			} catch (ClassNotFoundException | IOException | CoreServiceException | ComotException e) {
				e.printStackTrace();

			}
		}
	}

	protected void startIfActive(String instanceId) throws CoreServiceException, ComotException,
			ClassNotFoundException, IOException {

		CloudService servicefromInfo = infoService.getServiceInstance(instanceId);
		State state = lcManager.getCurrentState(instanceId, servicefromInfo.getId());

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
		if (binding1 != null) {
			admin.removeBinding(binding1);
		}
		if (binding2 != null) {
			admin.removeBinding(binding2);
		}
		if (binding3 != null) {
			admin.removeBinding(binding1);
		}
		if (binding4 != null) {
			admin.removeBinding(binding2);
		}
	}

}
