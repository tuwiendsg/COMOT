/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.core;

import java.util.List;

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
import at.ac.tuwien.dsg.comot.core.dal.ServiceRepo;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@Component
public class ComotOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(ComotOrchestrator.class);

	private static final long CHECK_STATE_TIMEOUT = 10000;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	@Autowired
	protected ServiceRepo serviceRepo;

	public List<ServiceEntity> getServices() {
		return (List<ServiceEntity>) serviceRepo.findAll();
	}
	

	public CloudService getStatus(String serviceId) throws CoreServiceException, ComotException {

		CloudService updated = deployment.refreshStatus(getServiceEntity(serviceId).getServiceDeployed());

		// TODO insert status from mela if monitored

		return updated;
	}
	
	public void deployNew(CloudService service) throws CoreServiceException, ComotException {

		CloudService deployedService = deployment.deploy(service);
		ServiceEntity entity = new ServiceEntity(service, deployedService);

		serviceRepo.save(entity);

	}

	public boolean startMonitoring(String serviceId) throws CoreServiceException,
			ComotException {

		if (!deployment.isRunning(serviceId)) {
			return false;
		}

		ServiceEntity entity = getServiceEntity(serviceId);
		CloudService service = deployment.refreshStatus(entity.getServiceDeployed());

		monitoring.startMonitoring(service, entity.getMcr());

		// TODO keep monitoring updated

		return true;

	}

	public boolean startControl(String serviceId)
			throws CoreServiceException,
			ComotException {

		if (!deployment.isRunning(serviceId)) {
			return false;
		}

		ServiceEntity entity = getServiceEntity(serviceId);
		CloudService service = deployment.refreshStatus(entity.getServiceDeployed());

		control.sendInitialConfig(service, entity.getMcr(), entity.getEffects());
		control.startControl(serviceId);

		return true;

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

	protected ServiceEntity getServiceEntity(String serviceId) {

		ServiceEntity entity = serviceRepo.findOne(serviceId);

		if (entity == null) {
			throw new ComotIllegalArgumentException("Cloud service with id '" + serviceId + "' does not exist");
		}

		return entity;
	}

}
