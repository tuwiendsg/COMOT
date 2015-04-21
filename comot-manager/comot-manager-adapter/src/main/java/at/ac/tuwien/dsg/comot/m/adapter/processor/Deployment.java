package at.ac.tuwien.dsg.comot.m.adapter.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

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

	protected Map<String, StatusTask> tasks = Collections.synchronizedMap(new HashMap<String, StatusTask>());

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
				instanceId + ".*.*.*." + Action.START + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.STOP + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.DEPLOYMENT_STARTED + "." + Type.SERVICE + "." + getId() + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.UNDEPLOYMENT_STARTED + "." + Type.SERVICE + "." + getId() + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + "." + EpsEvent.EPS_SUPPORT_REMOVED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + "." + EpsEvent.EPS_SUPPORT_ASSIGNED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_STARTED + ".*.#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_FINISHED + ".*.#"));
		bindings.add(bindingLifeCycle(queueName,
				"*.*.*.*." + Action.KILL + ".#"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws Exception {

		if (action == Action.START && !deployment.isManaged(instanceId)) {
			manager.sendLifeCycle(Type.SERVICE,
					new LifeCycleEvent(serviceId, instanceId, groupId, Action.DEPLOYMENT_STARTED));

		} else if (action == Action.STOP && deployment.isManaged(instanceId)) {
			manager.sendLifeCycle(Type.SERVICE,
					new LifeCycleEvent(serviceId, instanceId, groupId, Action.UNDEPLOYMENT_STARTED));

		} else if (action == Action.DEPLOYMENT_STARTED) {
			deployInstance(serviceId, instanceId);

		} else if (action == Action.UNDEPLOYMENT_STARTED) {

			unDeployInstance(serviceId, instanceId);

		} else if (action == Action.ELASTIC_CHANGE_STARTED) {

			if (!tasks.containsKey(instanceId)) {
				StatusTask task = new StatusTask(instanceId, groupId, helper.monitoringStatusUntilInterupted(serviceId,
						instanceId, service));
				tasks.put(instanceId, task);
			}

		} else if (action == Action.ELASTIC_CHANGE_FINISHED) {

			boolean stop = true;

			for (Transition transition : transitions.values()) {
				if (transition.getCurrentState() == State.ELASTIC_CHANGE) {
					stop = false;
				}
			}

			log.info("stop {}", stop);

			if (stop) {
				tasks.get(instanceId).getThread().cancel(true);
				tasks.remove(instanceId);
			}

		} else if (action == Action.KILL) {
			
			if (deployment.isManaged(instanceId)) {
				manager.sendLifeCycle(Type.SERVICE,
						new LifeCycleEvent(serviceId, instanceId, groupId, Action.UNDEPLOYMENT_STARTED));
			}
		}
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		EpsEvent action = EpsEvent.valueOf(event);

		if (action == EpsEvent.EPS_SUPPORT_REMOVED) {

			if (deployment.isManaged(instanceId)) {

				manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId,
						Action.UNDEPLOYMENT_STARTED));

				unDeployInstance(serviceId, instanceId);
			}
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId)
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

		manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.UNDEPLOYED));
	}

	public class StatusTask {

		String instanceId;
		Set<String> groupIds = new HashSet<>();
		Future thread;

		public StatusTask(String instanceId, String groupId, Future thread) {
			super();
			this.instanceId = instanceId;
			this.thread = thread;
		}

		public void addGroup(String groupId) {
			groupIds.add(groupId);
		}

		public void removeGroup(String groupId) {
			groupIds.remove(groupId);
		}

		public boolean stopIfNoGroup() {
			if (groupIds.isEmpty()) {
				thread.cancel(true);
				return true;
			} else {
				return false;
			}
		}

		public Future getThread() {
			return thread;
		}

		public void setThread(Future thread) {
			this.thread = thread;
		}

	}

}