package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycle;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleFactory;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.UtilsLc;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentHelper {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected DeploymentClient deployment;
	@Autowired
	protected DeploymentMapper mapper;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

	protected String adapterId;

	protected static final LifeCycle unitInstanceLc = LifeCycleFactory.getLifeCycle(Type.INSTANCE);

	public void setAdapterId(String adapterId) {
		this.adapterId = adapterId;
	}

	public void setDeployment(DeploymentClient deployment) {
		this.deployment = deployment;
	}

	@Async
	public void monitorStatusUntilDeployed(String serviceId, String instanceId, CloudService service)
			throws CoreServiceException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		try {

			Map<String, String> currentStates = new HashMap<>();
			Map<String, String> oldStates;

			State lcStateNew, lcStateOld;
			String stateNew;
			boolean notAllRunning = false;
			CloudService serviceReturned = service;
			Action action;

			service = UtilsLc.removeProviderInfo(service);

			do {

				try {

					oldStates = currentStates;
					currentStates = new HashMap<>();
					notAllRunning = false;

					Thread.sleep(1000);

					serviceReturned = deployment.refreshStatus(currentStates, service);
					serviceReturned.setId(serviceId);
					serviceReturned.setName(serviceId);

					// log.info("currentStates: {}", currentStates);

					if (currentStates.isEmpty()) {
						notAllRunning = true;
					}

					for (String uInstId : currentStates.keySet()) {

						stateNew = currentStates.get(uInstId);
						lcStateNew = DeploymentMapper.convert(currentStates.get(uInstId));

						if (lcStateNew != State.RUNNING) {
							notAllRunning = true;
						}

						// check if salsa state have changed
						if (oldStates.containsKey(uInstId)) {
							if (oldStates.get(uInstId).equals(stateNew)) {
								continue; // if not
							} else {
								lcStateOld = DeploymentMapper.convert(oldStates.get(uInstId));
							}
						} else {
							lcStateOld = State.INIT;
						}

						log.info("old: {}, new: {}", oldStates.get(uInstId), stateNew);

						// check if also the translated Life-cycle state have changed
						if (lcStateNew == lcStateOld) {

							lcManager.executeAction(new CustomEvent(serviceId, instanceId, uInstId, stateNew,
									adapterId, null, null));

						} else {

							if (lcStateNew == State.ERROR) {
								lcManager.executeAction(new LifeCycleEvent(serviceId, instanceId, uInstId,
										Action.ERROR,
										adapterId, serviceReturned));
								return;

							} else {
								action = unitInstanceLc.translateToAction(lcStateOld, lcStateNew);

								if (action == null) {
									log.error("invalid transitions {} -> {}", lcStateOld, lcStateNew);
								} else {
									lcManager.executeAction(new LifeCycleEvent(serviceId, instanceId, uInstId, action,
											adapterId, serviceReturned));
								}
							}
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

}
