/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.core;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.core.dal.JobRepo;
import at.ac.tuwien.dsg.comot.core.dal.ServiceRepo;
import at.ac.tuwien.dsg.comot.core.model.Job;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@Component
public class ComotOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(ComotOrchestrator.class);

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

	@Autowired
	protected AsyncExecutor executor;

	@PostConstruct
	public void setUp() {

		log.info("uuuu");
		executor.setOrchestrator(this);
		executor.start();
		log.info("uuuu2");
	}

	public List<ServiceEntity> getServices() {
		return (List<ServiceEntity>) serviceRepo.findAll();
	}

	public CloudService getStatus(String serviceId) throws CoreServiceException, ComotException {

		CloudService updated = getService(serviceId);

		// TODO insert status from mela if monitored

		return updated;
	}

	public void deployNew(CloudService service) throws CoreServiceException, ComotException {

		CloudService deployedService = deployment.deploy(service);
		ServiceEntity entity = new ServiceEntity(service, deployedService);

		serviceRepo.save(entity);

	}

	public void startMonitoring(String serviceId) throws CoreServiceException, ComotException {

		ServiceEntity entity = getServiceEntity(serviceId);

		if (deployment.isRunning(serviceId)) {

			CloudService service = deployment.refreshStatus(entity.getServiceDeployed());
			monitoring.startMonitoring(service, entity.getMcr());

			entity.setMonitoring(true);
			serviceRepo.save(entity);

			// TODO keep monitoring updated

		} else {
			jobRepo.save(new Job(Job.Type.START_MONITORING, entity));

			// entity.addJob(Job.Type.START_MONITORING);
			// serviceRepo.save(entity);
		}

	}

	public void startControl(String serviceId) throws CoreServiceException, ComotException {
		ServiceEntity entity = getServiceEntity(serviceId);

		if (deployment.isRunning(serviceId)) {

			CloudService service = deployment.refreshStatus(entity.getServiceDeployed());
			control.sendInitialConfig(service, entity.getMcr(), entity.getEffects());
			control.startControl(serviceId);

			// TODO keep control updated

		} else {
			jobRepo.save(new Job(Job.Type.START_CONTROL, entity));
		}

	}

	public void stopMonitoring(String serviceId) throws CoreServiceException, ComotException {

		ServiceEntity entity = getServiceEntity(serviceId);

		if (entity.getMonitoring()) {
			monitoring.stopMonitoring(serviceId);

			entity.setMonitoring(false);
			serviceRepo.save(entity);
		}

	}

	public void stopControl(String serviceId) throws CoreServiceException, ComotException {

		ServiceEntity entity = getServiceEntity(serviceId);

		if (entity.getControl()) {
			control.stopControl(serviceId);

			entity.setControl(false);
			serviceRepo.save(entity);
		}

	}

	public void setMcr(String serviceId, CompositionRulesConfiguration mcr) throws CoreServiceException,
			ComotException {

		ServiceEntity entity = getServiceEntity(serviceId);

		if (entity.getMonitoring()) {
			monitoring.setMcr(serviceId, mcr);
		}
		if (entity.getControl()) {
			if (entity.getMcr() == null) {
				control.createMcr(serviceId, mcr);
			} else {
				control.updateMcr(serviceId, mcr);
			}
		}

		entity.setMcr(mcr);
		serviceRepo.save(entity);
	}

	public void setEffects(String serviceId, String effects) throws CoreServiceException,
			ComotException {

		ServiceEntity entity = getServiceEntity(serviceId);

		if (entity.getControl()) {
			if (entity.getEffects() == null) {
				control.createEffects(serviceId, effects);
			} else {
				control.updateEffects(serviceId, effects);
			}
		}

		entity.setEffects(effects);
		serviceRepo.save(entity);
	}

	public CloudService getService(String serviceId) throws CoreServiceException, ComotException {

		ServiceEntity entity = getServiceEntity(serviceId);
		CloudService updated = deployment.refreshStatus(entity.getServiceDeployed());

		return updated;
	}

	public ServiceEntity getServiceEntity(String serviceId) {

		ServiceEntity entity = serviceRepo.findOne(serviceId);

		if (entity == null) {
			throw new ComotIllegalArgumentException("Cloud service with id '" + serviceId + "' does not exist");
		}

		return entity;
	}

}
