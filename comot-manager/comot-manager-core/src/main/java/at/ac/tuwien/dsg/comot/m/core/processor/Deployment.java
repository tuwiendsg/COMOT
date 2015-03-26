package at.ac.tuwien.dsg.comot.m.core.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
@Scope("prototype")
public class Deployment extends Processor {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected DeploymentHelper helper;

	protected Map<String, Future<Object>> tasks = Collections.synchronizedMap(new HashMap<String, Future<Object>>());

	@Override
	public void start() throws EpsException {

		helper.setDeploymentAdapter(this);
		helper.setAdapterId(getId());
		helper.setDeployment(deployment);

	}

	public void setHostAndPort(String host, int port) {
		deployment.setHostAndPort(host, port);
	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.STARTED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.STOPPED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.DEPLOYMENT_STARTED + "." + Type.SERVICE + "." + getId() + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.UNDEPLOYMENT_STARTED + "." + Type.SERVICE + "." + getId() + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + "." + EpsAction.EPS_ASSIGNMENT_REMOVED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + "." + EpsAction.EPS_ASSIGNED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_STARTED + ".*.#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_FINISHED + ".*.#"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws Exception {

		if (action == Action.STARTED && !deployment.isManaged(instanceId)) {
			manager.sendLifeCycle(Type.SERVICE,
					new LifeCycleEvent(serviceId, instanceId, groupId, Action.DEPLOYMENT_STARTED, getId()));

		} else if (action == Action.STOPPED) {
			manager.sendLifeCycle(Type.SERVICE,
					new LifeCycleEvent(serviceId, instanceId, groupId, Action.UNDEPLOYMENT_STARTED, getId()));

		} else if (action == Action.DEPLOYMENT_STARTED) {
			deployInstance(serviceId, instanceId);

		} else if (action == Action.UNDEPLOYMENT_STARTED) {

			unDeployInstance(serviceId, instanceId);

		} else if (action == Action.ELASTIC_CHANGE_STARTED) {

			tasks.put(uniqueTaskId(instanceId, groupId),
					helper.monitoringStatusUntilInterupted(serviceId, instanceId, service));

		} else if (action == Action.ELASTIC_CHANGE_FINISHED) {

			tasks.get(uniqueTaskId(instanceId, groupId)).cancel(true);

		}
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		EpsAction action = EpsAction.valueOf(event);

		if (action == EpsAction.EPS_ASSIGNMENT_REMOVED) {

			manager.removeInstanceListener(instanceId);

			if (deployment.isManaged(instanceId)) {

				manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId,
						Action.UNDEPLOYMENT_STARTED, getId()));

				unDeployInstance(serviceId, instanceId);
			}
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId, Exception e)
			throws Exception {
		// TODO Auto-generated method stub

	}

	protected void deployInstance(String serviceId, String instanceId) throws ClassNotFoundException, IOException,
			EpsException, ComotException, JAXBException, InterruptedException {

		// managedSet.add(instanceId);

		CloudService fullService = infoService.getServiceInstance(serviceId, instanceId);
		fullService.setId(instanceId);
		fullService.setName(instanceId);

		deployment.deploy(fullService);

		helper.monitorStatusUntilDeployed(serviceId, instanceId, fullService);

	}

	protected void unDeployInstance(String serviceId, String instanceId)
			throws EpsException, ClassNotFoundException, IOException, JAXBException {

		// managedSet.remove(instanceId);

		CloudService service = infoService.getServiceInstance(serviceId, instanceId);

		deployment.undeploy(instanceId);

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setInstances(new HashSet<UnitInstance>());
		}

		manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.UNDEPLOYED,
				getId()));
	}

	protected String uniqueTaskId(String instanceId, String groupId) {
		return instanceId + "_" + groupId;
	}

}
