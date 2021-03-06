/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator.interraction;

import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.CloudService;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.rsybl.rSYBLInterraction;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.salsa.SalsaInterraction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class COMOTOrchestrator {

    private SalsaInterraction salsaInterraction;
    private DefaultSalsaClient defaultSalsaClient;
    private rSYBLInterraction sYBLInterraction;

    {

        defaultSalsaClient = new DefaultSalsaClient();
        salsaInterraction = new SalsaInterraction().withDefaultSalsaClient(defaultSalsaClient);
        sYBLInterraction = new rSYBLInterraction();

    }

    public COMOTOrchestrator() {
    }

    public COMOTOrchestrator(String ip) {
        defaultSalsaClient.getConfiguration().setHost(ip);
        defaultSalsaClient.getConfiguration().setPort(8080);
        sYBLInterraction.setIp(ip);
        sYBLInterraction.setPort(8280);
    }

    public COMOTOrchestrator withIP(String ip) {
        defaultSalsaClient.getConfiguration().setHost(ip);
        sYBLInterraction.setIp(ip);
        return this;

    }

    public COMOTOrchestrator withSalsaIP(String ip) {
        defaultSalsaClient.getConfiguration().setHost(ip);

        return this;

    }

    public COMOTOrchestrator withSalsaPort(Integer port) {
        defaultSalsaClient.getConfiguration().setPort(port);
        return this;
    }

    public COMOTOrchestrator withRsyblIP(String ip) {
        sYBLInterraction.setIp(ip);
        return this;
    }

    public COMOTOrchestrator withRsyblPort(Integer port) {
        sYBLInterraction.setPort(port);
        return this;
    }

    public void deployAndControl(CloudService serviceTemplate) {

        defaultSalsaClient.deploy(serviceTemplate);
        salsaInterraction.waitUntilRunning(serviceTemplate.getId());

        DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

        sYBLInterraction.sendInitialConfigToRSYBL(serviceTemplate, deploymentDescription,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(), serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.capabilitiesToJSON(serviceTemplate));

    }

    public void deployAndMonitor(CloudService serviceTemplate) {

        defaultSalsaClient.deploy(serviceTemplate);
        salsaInterraction.waitUntilRunning(serviceTemplate.getId());

        DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

        sYBLInterraction.sendInitialConfigToRSYBL(serviceTemplate, deploymentDescription,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(), serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.capabilitiesToJSON(serviceTemplate));

        sYBLInterraction.stopControl(serviceTemplate);

    }

    public void controlExisting(CloudService serviceTemplate) {

        salsaInterraction.waitUntilRunning(serviceTemplate.getId());
        DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

        sYBLInterraction.sendInitialConfigToRSYBL(serviceTemplate, deploymentDescription,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(), serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.capabilitiesToJSON(serviceTemplate));

    }

    public void monitorExisting(CloudService serviceTemplate) {

        salsaInterraction.waitUntilRunning(serviceTemplate.getId());
        DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

        sYBLInterraction.sendInitialConfigToRSYBL(serviceTemplate, deploymentDescription,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(), serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.capabilitiesToJSON(serviceTemplate));

        sYBLInterraction.stopControl(serviceTemplate);

    }

    public void deploy(CloudService serviceTemplate) {

        defaultSalsaClient.deploy(serviceTemplate);
        //salsaInterraction.waitUntilRunning(serviceTemplate.getId());
        //DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

    }

    public void updateServiceReqsOrStruct(CloudService serviceTemplate) {

        sYBLInterraction.sendUpdatedConfigToRSYBL(serviceTemplate,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(), serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.capabilitiesToJSON(serviceTemplate)
        );

    }
    public void pauseControl(CloudService serviceTemplate){
        sYBLInterraction.stopControl(serviceTemplate);
    }
    public void resumeControl(CloudService serviceTemplate){
           DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

        sYBLInterraction.sendInitialConfigToRSYBL(serviceTemplate, deploymentDescription,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getId(), serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.capabilitiesToJSON(serviceTemplate));
    }

    public void getSalsaStatus(CloudService serviceTemplate) {

        DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

    }

    public COMOTOrchestrator withSalsaInterraction(final SalsaInterraction salsaInterraction) {
        this.salsaInterraction = salsaInterraction;
        return this;
    }

    public COMOTOrchestrator withDefaultSalsaClient(final DefaultSalsaClient defaultSalsaClient) {
        this.defaultSalsaClient = defaultSalsaClient;
        return this;
    }

    public COMOTOrchestrator withSYBLInterraction(final rSYBLInterraction sYBLInterraction) {
        this.sYBLInterraction = sYBLInterraction;
        return this;
    }

}
