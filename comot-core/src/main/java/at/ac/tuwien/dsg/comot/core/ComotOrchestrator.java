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

		List<ServiceEntity> list = (List<ServiceEntity>) serviceRepo.findAll();

		return list;
	}

	public CloudService getStatus(String serviceId) throws CoreServiceException, ComotException {

		CloudService updated = getServiceRefreshedStruxture(serviceId);

		// TODO insert status from mela if monitored

		return updated;
	}

	public void deployNew(CloudService service) throws CoreServiceException, ComotException {

		CloudService deployedService = deployment.deploy(service);

		ServiceEntity entity = new ServiceEntity(service, deployedService);

		serviceRepo.save(entity);

	}

	public void startMonitoring(String serviceId, CompositionRulesConfiguration mcr) throws CoreServiceException,
			ComotException {

		monitoring.startMonitoring(getServiceRefreshedStruxture(serviceId), mcr);

	}

	public void startControl(String serviceId, CompositionRulesConfiguration mcr, String effects)
			throws CoreServiceException,
			ComotException {

		control.sendInitialConfig(getServiceRefreshedStruxture(serviceId), mcr, effects);
		
		control.startControl(serviceId);

	}

	public void deployAndWait(CloudService service) throws CoreServiceException, ComotException {

		deployment.deploy(service);

		do {
			try {
				Thread.sleep(CHECK_STATE_TIMEOUT);
			} catch (InterruptedException ex) {
				log.error(ex.getMessage(), ex);
			}
		} while (!deployment.isRunning(service.getId()));

	}

	public void controlExisting(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException, ComotException {

		CloudService withStatus = deployment.refreshStatus(service);

		control.sendInitialConfig(withStatus, compositionRulesConfiguration,
				effectsJSON);

		control.startControl(service.getId());
	}

	public void deployAndControl(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException, ComotException {

		deployAndWait(service);

		controlExisting(service, compositionRulesConfiguration, effectsJSON);
	}

	public void updateServiceReqsOrStruct(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		control.sendUpdatedConfig(service, compositionRulesConfiguration, effectsJSON);

	}

	protected CloudService getServiceRefreshedStruxture(String serviceId) throws CoreServiceException, ComotException {

		ServiceEntity entity = serviceRepo.findOne(serviceId);

		if (entity == null) {
			return null;
		}

		return deployment.refreshStatus(entity.getServiceDeployed());

	}

}
