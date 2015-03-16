package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general.AdapterCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentAdapter extends AdapterCore {

	@Autowired
	protected DeploymentClient deployment;

	protected DeploymentHelper helper;

	public DeploymentAdapter() {
	}

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

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

		helper = context.getBean(DeploymentHelper.class);
		helper.setDeploymentAdapter(this);
		helper.setAdapterId(osuInstanceId);
		helper.setDeployment(deployment);

	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*." + State.STARTING + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*." + State.STOPPING + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + adapterId + "." + EpsAction.EPS_ASSIGNMENT_REMOVED + "." + Type.SERVICE));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + adapterId + "." + EpsAction.EPS_ASSIGNED + "." + Type.SERVICE));

		return bindings;
	}

	@Override
	protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws Exception {

		log.info("assigned");
		if (action == Action.STARTED && !deployment.isManaged(instanceId)) {
			log.info("STARTED");
			deployInstance(serviceId, instanceId);

		} else if (action == Action.STOPPED) {
			unDeployInstance(serviceId, instanceId);
		}

	}

	@Override
	protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String optionalMessage) throws Exception {

		EpsAction action = EpsAction.valueOf(event);

		if (action == EpsAction.EPS_ASSIGNED) {

			State state = msg.getTransitions().get(serviceId).getCurrentState();
			if (state == State.STARTING && !deployment.isManaged(instanceId)) {
				deployInstance(serviceId, instanceId);
			}

		} else if (action == EpsAction.EPS_ASSIGNMENT_REMOVED) {

			manager.removeInstanceListener(instanceId);

			if (deployment.isManaged(instanceId)) {

				sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STOPPED,
						adapterId, null));

				unDeployInstance(serviceId, instanceId);
			}

		}

	}

	protected void deployInstance(String serviceId, String instanceId) throws ClassNotFoundException, IOException,
			EpsException, ComotException, JAXBException, InterruptedException {

		// managedSet.add(instanceId);
		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.DEPLOYMENT_STARTED,
				adapterId, null));

		CloudService fullService = infoService.getServiceInstance(serviceId, instanceId);
		fullService.setId(instanceId);
		fullService.setName(instanceId);

		deployment.deploy(fullService);

		helper.monitorStatusUntilDeployed(serviceId, instanceId, fullService);

	}

	protected void unDeployInstance(String serviceId, String instanceId)
			throws EpsException, ClassNotFoundException, IOException, JAXBException {

		// managedSet.remove(instanceId);

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.UNDEPLOYMENT_STARTED,
				adapterId, null));

		CloudService service = infoService.getServiceInstance(serviceId, instanceId);

		deployment.undeploy(instanceId);

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setInstances(new HashSet<UnitInstance>());
		}

		sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.UNDEPLOYED,
				adapterId, service));
	}

}
