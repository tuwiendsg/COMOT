/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.client.SalsaStub;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentService;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class ComotOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(ComotOrchestrator.class);

	protected String monitoringIp = "localhost";
	protected String controlIp = "localhost";
	protected String deploymentIp = "localhost";
	protected int monitoringPort = 8180;
	protected int controlPort = 8280;
	protected int deploymentPort = 8080;

	protected SalsaInterraction salsaInterraction;
	protected rSYBLInterraction sYBLInterraction;
	protected DeploymentService depServ;

	protected boolean changedMon;
	protected boolean changedContr;
	protected boolean changedDepl;

	public ComotOrchestrator() {
		depServ = new SalsaStub(deploymentIp, deploymentPort);
		salsaInterraction = new SalsaInterraction(depServ);
		sYBLInterraction = new rSYBLInterraction();
	}

	public ComotOrchestrator withSalsaIP(String ip) {
		deploymentIp = ip;
		changedDepl = true;
		return this;

	}

	public ComotOrchestrator withSalsaPort(Integer port) {
		deploymentPort = port;
		changedDepl = true;
		return this;
	}

	public ComotOrchestrator withRsyblIP(String ip) {
		controlIp = ip;
		changedContr = true;
		return this;
	}

	public ComotOrchestrator withRsyblPort(Integer port) {
		controlPort = port;
		changedContr = true;
		return this;
	}

	protected void checkIfChanged() {
		
		if (changedDepl) {
			depServ.close();
			depServ = new SalsaStub(deploymentIp, deploymentPort);
			salsaInterraction.setDeploymentService(depServ);
		}
		
		if (changedContr) {

		}
	}

	public void deployAndControl(CloudService serviceTemplate) throws CoreServiceException {

		depServ.deploy(serviceTemplate);
		salsaInterraction.waitUntilRunning(serviceTemplate.getId());

		DeploymentDescription deploymentDescription = depServ.getServiceDeploymentInfo(serviceTemplate.getId());

		log.info("deploymentDescription: " + deploymentDescription);

		sYBLInterraction.sendInitialConfigToRSYBL(
				serviceTemplate,
				deploymentDescription,
				sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(),
						serviceTemplate.getMetricCompositonRulesFile()),
				sYBLInterraction.loadJSONEffects(serviceTemplate.getEffectsCompositonRulesFile()));

	}

	public void deploy(CloudService serviceTemplate) throws CoreServiceException {

		depServ.deploy(serviceTemplate);
		salsaInterraction.waitUntilRunning(serviceTemplate.getId());
		DeploymentDescription deploymentDescription = depServ.getServiceDeploymentInfo(serviceTemplate.getId());

		log.info("deploymentDescription: " + deploymentDescription);
	}

	public void updateServiceReqsOrStruct(CloudService serviceTemplate) {

		sYBLInterraction.sendUpdatedConfigToRSYBL(
				serviceTemplate,
				sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(),
						serviceTemplate.getMetricCompositonRulesFile()),
				sYBLInterraction.loadJSONEffects(serviceTemplate.getEffectsCompositonRulesFile())
				);

	}

	public void controlExisting(CloudService serviceTemplate) throws CoreServiceException {

		DeploymentDescription deploymentDescription = depServ.getServiceDeploymentInfo(serviceTemplate.getId());

		sYBLInterraction.sendInitialConfigToRSYBL(
				serviceTemplate,
				deploymentDescription,
				sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(),
						serviceTemplate.getMetricCompositonRulesFile()),
				sYBLInterraction.loadJSONEffects(serviceTemplate.getEffectsCompositonRulesFile()));

	}

	public void getSalsaStatus(CloudService serviceTemplate) throws CoreServiceException {

		DeploymentDescription deploymentDescription = depServ.getServiceDeploymentInfo(serviceTemplate.getId());

	}

}
