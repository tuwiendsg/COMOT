/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@Component
public class ComotOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(ComotOrchestrator.class);

	private static final long CHECK_STATE_TIMEOUT = 10000;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control; 

	public ComotOrchestrator() {
		
	}

	public ComotOrchestrator withSalsaIP(String ip) {
		deployment.setHost(ip);
		return this;

	}

	public ComotOrchestrator withSalsaPort(Integer port) {
		deployment.setPort(port);
		return this;
	}

	public ComotOrchestrator withRsyblIP(String ip) {
		control.setHost(ip);
		return this;
	}

	public ComotOrchestrator withRsyblPort(Integer port) {
		control.setPort(port);
		return this;
	}

	// /////////////////////

	public void deploy(CloudService serviceTemplate) throws CoreServiceException, ComotException {

		deployment.deploy(serviceTemplate);

		do {
			try {
				Thread.sleep(CHECK_STATE_TIMEOUT);
			} catch (InterruptedException ex) {
				log.error(ex.getMessage(), ex);
			}
		} while (!deployment.isRunning(serviceTemplate.getId()));

	}
	
	public void controlExisting(
			CloudService serviceTemplate,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException, ComotException {

		CloudService withStatus = deployment.getStatus(serviceTemplate);
		
		control.sendInitialConfig(withStatus, compositionRulesConfiguration,
				effectsJSON);
		
		control.startControl(serviceTemplate.getId());
	}

	public void deployAndControl(
			CloudService serviceTemplate,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException, ComotException {

		deploy(serviceTemplate);

		controlExisting(serviceTemplate, compositionRulesConfiguration, effectsJSON);
	}

	public void updateServiceReqsOrStruct(
			CloudService serviceTemplate,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		control.sendUpdatedConfig(serviceTemplate, compositionRulesConfiguration, effectsJSON);

	}

}
