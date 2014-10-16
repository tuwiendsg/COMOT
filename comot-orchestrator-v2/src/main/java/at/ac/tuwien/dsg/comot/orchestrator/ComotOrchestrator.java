/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.client.RsyblService;
import at.ac.tuwien.dsg.comot.client.stub.RsyblStub;
import at.ac.tuwien.dsg.comot.client.stub.SalsaStub;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ComotOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(ComotOrchestrator.class);

	protected String monitoringIp = "localhost";
	protected String controlIp = "localhost";
	protected String deploymentIp = "localhost";
	protected int monitoringPort = 8180;
	protected int controlPort = 8280;
	protected int deploymentPort = 8080;

	protected SalsaInterraction salsaInterraction;
	protected RsyblService rsyblService;
	protected SalsaStub salsaStub;
	private RsyblStub rsybl;

	protected boolean changedMon;
	protected boolean changedContr;
	protected boolean changedDepl;

	public ComotOrchestrator() {
		salsaStub = new SalsaStub(deploymentIp, deploymentPort);
		salsaInterraction = new SalsaInterraction(salsaStub);
		rsybl = new RsyblStub(controlIp, controlPort);
		rsyblService = new RsyblService(rsybl);
	}

	public ComotOrchestrator withSalsaIP(String ip) {
		salsaStub.setHost(ip);
		return this;

	}

	public ComotOrchestrator withSalsaPort(Integer port) {
		salsaStub.setPort(port);
		return this;
	}

	public ComotOrchestrator withRsyblIP(String ip) {
		rsybl.setHost(ip);
		return this;
	}

	public ComotOrchestrator withRsyblPort(Integer port) {
		rsybl.setPort(port);
		return this;
	}

	// /////////////////////

	public void deploy(CloudService serviceTemplate) throws CoreServiceException {

		salsaStub.deploy(serviceTemplate);
		salsaInterraction.waitUntilRunning(serviceTemplate.getId());
		DeploymentDescription deploymentDescription = salsaStub.getServiceDeploymentInfo(serviceTemplate.getId());

	}

	public void deployAndControl(
			CloudService serviceTemplate,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		salsaStub.deploy(serviceTemplate);
		salsaInterraction.waitUntilRunning(serviceTemplate.getId());

		DeploymentDescription deploymentDescription = salsaStub.getServiceDeploymentInfo(serviceTemplate.getId());

		rsyblService.sendInitialConfig(serviceTemplate, deploymentDescription, compositionRulesConfiguration,
				effectsJSON);
	}

	public void updateServiceReqsOrStruct(
			CloudService serviceTemplate,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		rsyblService.sendUpdatedConfig(serviceTemplate, compositionRulesConfiguration, effectsJSON);

	}

	public void controlExisting(
			CloudService serviceTemplate,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		DeploymentDescription deploymentDescription = salsaStub.getServiceDeploymentInfo(serviceTemplate.getId());

		rsyblService.sendInitialConfig(serviceTemplate, deploymentDescription, compositionRulesConfiguration,
				effectsJSON);
	}

}
