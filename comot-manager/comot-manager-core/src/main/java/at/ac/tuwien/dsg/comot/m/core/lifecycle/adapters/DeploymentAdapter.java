package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentAdapter extends Adapter {

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected DeploymentMapper mapper;
	@Autowired
	protected DeploymentHelper helper;

	public DeploymentAdapter() {
	}

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

		deployment.setHostAndPort(ip, new Integer(port));

		helper.setAdapterId(osuInstanceId);
		helper.setDeployment(deployment);

		bindingLifeCycle("*.TRUE.*." + State.STARTING + ".#");
		bindingLifeCycle("*.TRUE.*." + State.STOPPING + ".#");
		bindingCustom("*." + adapterId + "." + EpsAction.EPS_ASSIGNMENT_REMOVED + ".SERVICE");
		bindingCustom("*." + adapterId + "." + EpsAction.EPS_ASSIGNED + ".SERVICE");

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

			if (infoService.isOsuAssignedToInstance(serviceId, instanceId, adapterId)) {

				log.info("assigned");
				if (action == Action.STARTED && !deployment.isManaged(instanceId)) {
					log.info("STARTED");
					deployInstance(serviceId, instanceId);

				} else if (action == Action.STOPPED) {
					unDeployInstance(serviceId, instanceId);
				}
			}
		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String epsId, String optionalMessage) throws ClassNotFoundException, IOException,
				JAXBException,
				CoreServiceException, ComotException, InterruptedException {

			if (adapterId.equals(epsId)) {
				EpsAction action = EpsAction.valueOf(event);

				if (action == EpsAction.EPS_ASSIGNED) {

					State state = msg.getTransitions().get(serviceId).getCurrentState();
					if (state == State.STARTING && !deployment.isManaged(instanceId)) {
						deployInstance(serviceId, instanceId);
					}

				} else if (action == EpsAction.EPS_ASSIGNMENT_REMOVED) {

					infoService.removeAssignmentOfSupportingOsu(serviceId, instanceId, adapterId);

					LifeCycleEvent newEvent = new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STOPPED,
							adapterId, null);
					lcManager.executeAction(newEvent);

					if (deployment.isManaged(instanceId)) {

						unDeployInstance(serviceId, instanceId);
					}
				}
			}
		}

	}

	protected void atStartUpDeployWhatIsWaiting() {

		Map<String, String> instances = infoService.getInstancesHavingThisOsuAssigned(adapterId);

		for (String instanceId : instances.keySet()) {

			String serviceId = instances.get(instanceId);
			State state = lcManager.getCurrentState(instanceId, serviceId);

			if (state == State.STARTING) {
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

		// managedSet.add(instanceId);

		lcManager.executeAction(new LifeCycleEvent(serviceId, instanceId, serviceId, Action.DEPLOYMENT_STARTED,
				adapterId, null));

		CloudService fullService = infoService.getServiceInstance(serviceId, instanceId);
		fullService.setId(instanceId);
		fullService.setName(instanceId);

		deployment.deploy(fullService);

		helper.monitorStatusUntilDeployed(serviceId, instanceId, fullService);

	}

	protected void unDeployInstance(String serviceId, String instanceId)
			throws CoreServiceException, ClassNotFoundException, IOException, JAXBException {

		// managedSet.remove(instanceId);

		lcManager.executeAction(new LifeCycleEvent(serviceId, instanceId, serviceId, Action.UNDEPLOYMENT_STARTED,
				adapterId, null));

		CloudService service = infoService.getServiceInstance(serviceId, instanceId);

		deployment.undeploy(instanceId);

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setInstances(new HashSet<UnitInstance>());
		}

		lcManager.executeAction(new LifeCycleEvent(serviceId, instanceId, serviceId, Action.UNDEPLOYED,
				adapterId, service));
	}

	@Override
	protected void clean() {

	}

}
