package at.ac.tuwien.dsg.comot.m.adapter.processor;

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
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentHelper {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static long REFRESH_INTERVAL = 1000;

	protected DeploymentClient deployment;
	protected Deployment dAdapt;
	protected Manager manager;

	@Autowired
	protected DeploymentMapper mapper;
	@Autowired
	protected InformationClient infoService;

	protected String adapterId;

	@Async
	public void monitorStatusUntilDeployed(String serviceId, String instanceId, CloudService service)
			throws EpsException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		try {

			Map<String, String> currentStates = new HashMap<>();
			Memory memory = new Memory();
			boolean notAllRunning = false;

			UtilsLc.removeProviderInfo(service);

			do {
				try {

					notAllRunning = false;
					memory.refresh(currentStates);
					currentStates = oneInteration(memory, serviceId, instanceId, service);

					if (currentStates.isEmpty()) {
						notAllRunning = true;
					}

					for (String state : currentStates.values()) {
						if (DeploymentMapper.convert(state) != State.RUNNING) {
							notAllRunning = true;
						}
					}

				} catch (ComotException e) {
					log.warn(e.getMessage());
				}

			} while (notAllRunning);
			log.info("stopped checking");
		} catch (Throwable e) {
			log.error("{}", e);
		}

	}

	@Async
	public Future<Object> monitoringStatusUntilInterupted(String serviceId, String instanceId, CloudService service)
			throws EpsException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		log.info("monitoringStatusUntilInterupted(instanceId={})", instanceId);

		try {

			Map<String, String> currentStates = new HashMap<>();
			Memory memory = new Memory();

			UtilsLc.removeProviderInfo(service);
			service.setId(instanceId);
			service.setName(instanceId);

			Set<UnitInstance> oldInstances = service.getInstancesList().get(0).getUnitInstances();

			for (UnitInstance inst : oldInstances) {
				currentStates.put(inst.getId(), DeploymentMapper.runningToState());
			}

			do {
				try {

					memory.refresh(currentStates);
					currentStates = oneInteration(memory, serviceId, instanceId, service);

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
			log.error("{}", e);
		}
		return null;

	}

	protected Map<String, String> oneInteration(Memory memory, String serviceId, String instanceId, CloudService service)
			throws EpsException, ComotException, InterruptedException, AmqpException, JAXBException {

		Map<String, String> currentStates = new HashMap<>();
		CloudService serviceReturned;
		State lcStateNew;
		String stateNew;

		currentStates = new HashMap<>();

		Thread.sleep(REFRESH_INTERVAL);

		serviceReturned = deployment.refreshStatus(currentStates, service);
		serviceReturned.setId(serviceId);
		serviceReturned.setName(serviceId);

		log.trace("currentStates: {}", currentStates);

		for (String uInstId : currentStates.keySet()) {

			stateNew = currentStates.get(uInstId);
			lcStateNew = DeploymentMapper.convert(currentStates.get(uInstId));

			evaluateChangeOfOneUnitInstance(serviceId, instanceId, uInstId, stateNew, lcStateNew,
					serviceReturned, memory);
		}

		return currentStates;

	}

	protected void evaluateChangeOfOneUnitInstance(
			String serviceId, String instanceId, String uInstId, String stateNew, State lcStateNew,
			CloudService serviceReturned, Memory memory)
			throws AmqpException, JAXBException {

		Action action;
		State lcStateOld;

		if (memory.contains(uInstId)) {
			if (memory.oldSalas(uInstId).equals(stateNew)) {
				return;
			} else {
				lcStateOld = DeploymentMapper.convert(memory.oldSalas(uInstId));
				if (lcStateOld == null) {
					lcStateOld = memory.oldLc(uInstId);
				}
			}
		} else {
			lcStateOld = State.INIT;
		}

		log.info("{} old {}={}, new: {}={}", uInstId, memory.oldSalas(uInstId), lcStateOld, stateNew,
				lcStateNew);

		// check if also the translated Life-cycle state have changed
		if (lcStateNew == null || lcStateNew == lcStateOld) {

			manager.sendCustom(Type.INSTANCE,
					new CustomEvent(serviceId, instanceId, uInstId, stateNew, adapterId, null));

		} else {

			if (lcStateNew == State.ERROR) {
				manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, instanceId, uInstId, Action.ERROR));
				return;

			} else {

				if (lcStateOld == State.INIT && lcStateNew == State.DEPLOYING) {
					action = Action.DEPLOYMENT_STARTED;
				} else if (lcStateOld == State.DEPLOYING && lcStateNew == State.RUNNING) {
					action = Action.DEPLOYED;
					// } else if (lcStateOld == State.RUNNING && lcStateNew == State.UNDEPLOYING) {
					// action = Action.UNDEPLOYMENT_STARTED;
					// } else if (lcStateOld == State.UNDEPLOYING && lcStateNew == State.FINAL) {
					// action = Action.UNDEPLOYED;
				} else {
					log.error("invalid transitions {} -> {}", lcStateOld, lcStateNew);
					return;
				}

				Navigator nav = new Navigator(serviceReturned);

				log.info("creating ModifyingLifeCycleEvent for: {}, navigator: {}", uInstId, nav);

				manager.sendLifeCycle(Type.INSTANCE,
						new LifeCycleEventModifying(serviceId, instanceId, uInstId, action, adapterId,
								System.currentTimeMillis(), nav.getUnitFor(uInstId).getId(), nav.getInstance(uInstId)));
			}
		}
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

	public class Memory {

		Map<String, String> oldStates;
		Map<String, State> oldStatesLc = new HashMap<>();

		public void refresh(Map<String, String> currentStates) {

			oldStates = currentStates;

			State temp;
			for (String uInstId : currentStates.keySet()) {
				temp = DeploymentMapper.convert(oldStates.get(uInstId));
				if (temp != null) {
					oldStatesLc.put(uInstId, temp);
				}
			}
			log.trace("oldStates {}", oldStates);
			log.trace("oldStatesLc {}", oldStatesLc);
		}

		public boolean contains(String uInstId) {
			return oldStates.containsKey(uInstId);
		}

		public String oldSalas(String uInstId) {
			return oldStates.get(uInstId);
		}

		public State oldLc(String uInstId) {
			return oldStatesLc.get(uInstId);
		}

	}

}