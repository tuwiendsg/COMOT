package at.ac.tuwien.dsg.comot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.core.dal.JobRepo;
import at.ac.tuwien.dsg.comot.core.dal.ServiceRepo;
import at.ac.tuwien.dsg.comot.core.model.Job;
import at.ac.tuwien.dsg.comot.core.model.Job.Type;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;

@Component
public class AsyncExecutor {

	private static final Logger log = LoggerFactory.getLogger(AsyncExecutor.class);

	private static final long TIMEOUT = 10000;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	@Autowired
	protected ServiceRepo serviceRepo;
	@Autowired
	protected JobRepo jobRepo;

	protected ComotOrchestrator orchestrator;

	@Async
	public void start() {

		while (true) {
			log.trace("async executor, timeout {} ms", TIMEOUT);

			oneIteration();

			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException e) {
				log.info("Failed to sleep ", e);
			}
		}
	}

	protected void oneIteration() {

		for (Job job : jobRepo.findAll()) {

			try {

				ServiceEntity entity = job.getService();
				String serviceId = entity.getId();

				if (deployment.isRunning(serviceId)) {

					if (job.getType().equals(Type.START_MONITORING)) {
						log.debug("Executing {}", job);
						monitoring.startMonitoring(orchestrator.getService(serviceId), entity.getMcr());

						entity.setMonitoring(true);
						serviceRepo.save(entity);

						jobRepo.delete(job.getId());

					} else if (job.getType().equals(Type.START_CONTROL)) {
						log.debug("Executing {}", job);
						control.sendInitialConfig(orchestrator.getService(serviceId), entity.getMcr(),
								entity.getEffects());
						control.startControl(serviceId);

						entity.setControl(true);
						serviceRepo.save(entity);

						jobRepo.delete(job.getId());
					} else if (job.getType().equals(Type.UPDATE_STRUCTURE_MONITORING)) {
						log.debug("Executing {}", job);
						monitoring.updateService(serviceId, orchestrator.getService(serviceId));
					}
				}

			} catch (Throwable e) {
				log.error("Failed to execute job '" + job.getId() + "' of type '" + job.getType() + "' ", e);
			}
		}
	}

	public ComotOrchestrator getOrchestrator() {
		return orchestrator;
	}

	public void setOrchestrator(ComotOrchestrator orchestrator) {
		this.orchestrator = orchestrator;
	}

}
