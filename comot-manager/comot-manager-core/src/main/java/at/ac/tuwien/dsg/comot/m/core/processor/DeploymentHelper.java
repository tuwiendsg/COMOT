package at.ac.tuwien.dsg.comot.m.core.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ModifyingLifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycle;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleFactory;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentHelper {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected DeploymentClient deployment;
	protected Deployment dAdapt;
	protected Manager manager;

	@Autowired
	protected DeploymentMapper mapper;
	@Autowired
	protected InformationClient infoService;

	protected String adapterId;

	protected static final LifeCycle unitInstanceLc = LifeCycleFactory.getLifeCycle(Type.INSTANCE);

	@Async
	public void monitorStatusUntilDeployed(String serviceId, String instanceId, CloudService service)
			throws EpsException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		try {

			Map<String, String> currentStates = new HashMap<>();
			Map<String, String> oldStates;

			State lcStateNew, lcStateOld;
			String stateNew;
			boolean notAllRunning = false;
			CloudService serviceReturned;

			UtilsLc.removeProviderInfo(service);

			do {

				try {

					oldStates = currentStates;
					currentStates = new HashMap<>();
					notAllRunning = false;

					Thread.sleep(2000);

					serviceReturned = deployment.refreshStatus(currentStates, service);
					serviceReturned.setId(serviceId);
					serviceReturned.setName(serviceId);

					log.trace("currentStates: {}", currentStates);

					if (currentStates.isEmpty()) {
						notAllRunning = true;
					}

					for (String uInstId : currentStates.keySet()) {

						stateNew = currentStates.get(uInstId);
						lcStateNew = DeploymentMapper.convert(currentStates.get(uInstId));

						if (lcStateNew != State.RUNNING) {
							notAllRunning = true;
						}

						lcStateOld = getChangedSalsaState(uInstId, stateNew, oldStates);

						// check if salsa state have changed
						if (lcStateOld == null) {
							continue;
						} else {
							evaluateChangeOfOneUnitInstance(serviceId, instanceId, uInstId, stateNew, lcStateOld,
									lcStateNew,
									serviceReturned);

						}

					}

				} catch (ComotException e) {
					log.warn(e.getMessage());
				}

			} while (notAllRunning);

			log.info("stopped checking");

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	protected State getChangedSalsaState(String uInstId, String stateNew, Map<String, String> oldStates) {

		State lcStateOld;

		if (oldStates.containsKey(uInstId)) {
			if (oldStates.get(uInstId).equals(stateNew)) {
				return null; // if not
			} else {
				lcStateOld = DeploymentMapper.convert(oldStates.get(uInstId));
			}
		} else {
			lcStateOld = State.INIT;
		}

		log.info("old: {}, new: {}", oldStates.get(uInstId), stateNew);
		return lcStateOld;
	}

	protected void evaluateChangeOfOneUnitInstance(
			String serviceId, String instanceId, String uInstId,
			String stateNew, State lcStateOld, State lcStateNew, CloudService serviceReturned)
			throws AmqpException, JAXBException {

		Action action;

		// check if also the translated Life-cycle state have changed
		if (lcStateNew == lcStateOld) {

			manager.sendCustom(Type.INSTANCE,
					new CustomEvent(serviceId, instanceId, uInstId, stateNew, adapterId, null));

		} else {

			if (lcStateNew == State.ERROR) {
				manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, instanceId, uInstId, Action.ERROR));
				return;

			} else {
				action = unitInstanceLc.translateToAction(lcStateOld, lcStateNew);

				Navigator nav = new Navigator(serviceReturned);

				if (action == null) {
					log.error("invalid transitions {} -> {}", lcStateOld, lcStateNew);
				} else {
					log.info("creating ModifyingLifeCycleEvent for: {}, navigator: {}", uInstId, nav);

					manager.sendLifeCycle(
							Type.INSTANCE,
							new ModifyingLifeCycleEvent(serviceId, instanceId, uInstId,
									action, adapterId, System.currentTimeMillis(), nav.getUnitFor(uInstId).getId(), nav
											.getInstance(uInstId)));
				}
			}
		}
	}

	@Async
	public Future<Object> monitoringStatusUntilInterupted(String serviceId, String instanceId, CloudService service)
			throws EpsException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		try {

			Map<String, String> currentStates = new HashMap<>();
			Map<String, String> oldStates;

			State lcStateNew, lcStateOld;
			String stateNew;
			CloudService serviceReturned;

			UtilsLc.removeProviderInfo(service);
			service.setId(instanceId);
			service.setName(instanceId);

			Set<UnitInstance> oldInstances = service.getInstancesList().get(0).getUnitInstances();

			for (UnitInstance inst : oldInstances) {
				currentStates.put(inst.getId(), DeploymentMapper.runningToState());
			}

			do {

				try {

					oldStates = currentStates;
					currentStates = new HashMap<>();

					Thread.sleep(1000);

					serviceReturned = deployment.refreshStatus(currentStates, service);
					serviceReturned.setId(serviceId);
					serviceReturned.setName(serviceId);

					log.trace("currentStates: {}", currentStates);

					for (String uInstId : currentStates.keySet()) {

						stateNew = currentStates.get(uInstId);

						lcStateNew = DeploymentMapper.convert(currentStates.get(uInstId));
						lcStateOld = getChangedSalsaState(uInstId, stateNew, oldStates);

						// check if salsa state have changed
						if (lcStateOld == null) {
							continue;
						} else {
							evaluateChangeOfOneUnitInstance(serviceId, instanceId, uInstId, stateNew, lcStateOld,
									lcStateNew,
									serviceReturned);
						}
					}

					for (Iterator<UnitInstance> iterator = oldInstances.iterator(); iterator.hasNext();) {
						UnitInstance inst = iterator.next();

						if (!currentStates.containsKey(inst.getId())) {

							manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, instanceId,
									inst.getId(), Action.UNDEPLOYMENT_STARTED));
							manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, instanceId,
									inst.getId(), Action.UNDEPLOYED));

							iterator.remove();
						}
					}

				} catch (ComotException e) {
					log.warn(e.getMessage());
				}

			} while (true);

		} catch (InterruptedException e) {
			log.info("Task interrupted as expected");

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;

	}

	public void setAdapterId(String adapterId) {
		this.adapterId = adapterId;
	}

	public void setDeployment(DeploymentClient deployment) {
		this.deployment = deployment;
	}

	public void setDeploymentAdapter(Deployment dAdapt) {
		this.dAdapt = dAdapt;
		this.manager = dAdapt.getManager();
	}

}
