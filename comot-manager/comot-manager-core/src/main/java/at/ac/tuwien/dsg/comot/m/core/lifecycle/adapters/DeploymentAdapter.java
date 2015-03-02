package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.UtilsLc;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
// @Scope("prototype") //for some reason this creates multip0le instances
public class DeploymentAdapter extends Adapter {

	@Autowired
	protected DeploymentClient deployment;

	protected Binding binding1;
	protected Binding binding2;
	protected Binding binding3;

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

		deployment.setHost(ip);
		deployment.setPort(new Integer(port));

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.STARTING + ".#", null);
		binding2 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.STOPPING + ".#", null);
		binding3 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				"*." + EpsAction.EPS_ASSIGNMENT_REMOVED + ".SERVICE", null);

		admin.declareBinding(binding1);
		admin.declareBinding(binding2);
		admin.declareBinding(binding3);

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
				throws ClassNotFoundException, CoreServiceException, IOException, JAXBException, ComotException,
				InterruptedException {

			if (isAssignedTo(serviceId, instanceId)) {
				if (action.equals(Action.STARTED)) {
					deployInstance(serviceId, instanceId);

				} else if (action.equals(Action.STOPPED)) {
					unDeployInstance(serviceId, instanceId, service);
				}
			}
		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String optionalMessage) throws ClassNotFoundException, IOException, JAXBException {

			if (isAssignedTo(serviceId, instanceId)) {

				EpsAction action = EpsAction.valueOf(event);

				log.info("action {} {}", action, EpsAction.EPS_ASSIGNMENT_REMOVED);

				if (action.equals(EpsAction.EPS_ASSIGNMENT_REMOVED)) {
					log.info("go");
					EventMessage newEvent = new EventMessage(serviceId, instanceId, serviceId, Action.STOPPED, null,
							null);
					lcManager.executeAction(newEvent);
				}
			}
		}

	}

	protected void atStartUpDeployWhatIsWaiting() {

		Map<String, String> instances = infoService.getInstancesHavingThisOsuAssigned(adapterId);

		for (String instanceId : instances.keySet()) {

			State state = lcManager.getCurrentState(instanceId, instances.get(instanceId));

			if (state.equals(State.STARTING)) {
				try {
					deployInstance(instances.get(instanceId), instanceId);
				} catch (ClassNotFoundException | IOException | CoreServiceException | ComotException | JAXBException
						| InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void deployInstance(String serviceId, String instanceId) throws ClassNotFoundException, IOException,
			CoreServiceException, ComotException, JAXBException, InterruptedException {

		CloudService fullService = infoService.getServiceInstance(serviceId, instanceId);
		fullService.setId(instanceId);
		fullService.setName(instanceId);

		deployment.deploy(fullService);

		monitorStatusUntilDeployed(serviceId, fullService);
	}

	protected void unDeployInstance(String serviceId, String instanceId, CloudService service)
			throws CoreServiceException, ClassNotFoundException, IOException, JAXBException {

		deployment.undeploy(instanceId);

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setInstances(new HashSet<UnitInstance>());
		}

		lcManager.executeAction(new EventMessage(serviceId, instanceId, serviceId, Action.UNDEPLOYED,
				service, null));
	}

	protected void assignmentRemoved() {
		// TODO
	}

	protected void monitorStatusUntilDeployed(String serviceId, CloudService service) throws CoreServiceException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		Map<String, State> map;
		State oldState, newState;
		boolean notAllRunning = false;
		String uInstId;
		CloudService serviceReturned = service;

		service = UtilsLc.removeProviderInfo(service);

		do {

			try {

				map = new HashMap<>();
				for (ServiceUnit unit : Navigator.getAllUnits(serviceReturned)) {
					for (UnitInstance instance : unit.getInstances()) {
						map.put(instance.getId(), instance.getState());
						// log.info("xxxx: {} {}", instance.getId(), instance.getState());
					}
				}
				notAllRunning = false;

				Thread.sleep(1000);

				serviceReturned = deployment.refreshStatus(service);
				serviceReturned.setId(serviceId);
				serviceReturned.setName(serviceId);

				for (ServiceUnit unit : Navigator.getAllUnits(serviceReturned)) {
					for (UnitInstance instance : unit.getInstances()) {
						uInstId = instance.getId();
						newState = instance.getState();

						if (!State.OPERATION_RUNNING.equals(newState)) {
							notAllRunning = true;
						}

						if (map.containsKey(uInstId)) {
							if (map.get(uInstId).equals(newState)) {
								continue;
							} else {
								oldState = map.get(uInstId);
							}
						} else {
							oldState = State.STARTING;
						}

						if (State.ERROR.equals(newState)) {
							// TODO process error
							log.error("error ocured");
						}

						// publish
						Action action = UtilsLc.translateToAction(oldState, newState);

						if (action == null) {
							log.error("invalid transitions {} -> {}", oldState, newState);
						} else {
							lcManager.executeAction(new EventMessage(serviceId, service.getId(), uInstId, action,
									serviceReturned, null));
						}
					}
					if (unit.getInstances().isEmpty()) {
						notAllRunning = true;
					}
				}

			} catch (ComotException e) {
				log.warn(e.getMessage());
			}

		} while (notAllRunning);

		log.info("stopped checking");

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
			admin.removeBinding(binding3);
		}
	}

}
