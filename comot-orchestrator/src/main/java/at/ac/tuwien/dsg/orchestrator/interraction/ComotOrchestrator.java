/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.orchestrator.interraction;

import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import at.ac.tuwien.dsg.comot.common.model.ServiceTemplate;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.orchestrator.interraction.rsybl.RsyblConnector;
import at.ac.tuwien.dsg.orchestrator.interraction.salsa.SalsaConnector;

import java.util.logging.Level;
import java.util.logging.Logger;

import static at.ac.tuwien.dsg.comot.common.model.CloudApplication.CloudApplication;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class ComotOrchestrator {

    private SalsaConnector salsaConnector;
    private DefaultSalsaClient defaultSalsaClient;
    private RsyblConnector sYBLInterraction;

    {

        defaultSalsaClient = new DefaultSalsaClient();
        salsaConnector = new SalsaConnector().withDefaultSalsaClient(defaultSalsaClient);
        sYBLInterraction = new RsyblConnector();

    }

    public ComotOrchestrator withSalsaIP(String ip) {
        defaultSalsaClient.getConfiguration().setHost(ip);

        return this;

    }

    public ComotOrchestrator withSalsaPort(Integer port) {
        defaultSalsaClient.getConfiguration().setPort(port);
        return this;
    }

    public ComotOrchestrator withRsyblIP(String ip) {
        sYBLInterraction.setIp(ip);
        return this;
    }

    public ComotOrchestrator withRsyblPort(Integer port) {
        sYBLInterraction.setPort(port);
        return this;
    }

    public void deployAndControl(ServiceTemplate serviceTemplate) {

        CloudApplication application = CloudApplication(serviceTemplate.getId()).withName(serviceTemplate.getId()).consistsOfServices(serviceTemplate).withDefaultMetricsEnabled(true);

        defaultSalsaClient.deploy(application);
        salsaConnector.waitUntilRunning(serviceTemplate.getId());
        
        try {
            //wait 30 seconds more
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ComotOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
        }

        DeploymentDescription deploymentDescription = salsaConnector.getServiceDeploymentInfo(serviceTemplate.getId());

        sYBLInterraction.sendInitialConfigToRSYBL(serviceTemplate, deploymentDescription,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.loadJSONEffects(serviceTemplate.getEffectsCompositonRulesFile()));

    }

    public void controlExisting(ServiceTemplate serviceTemplate) {

        sYBLInterraction.sendUpdatedConfigToRSYBL(serviceTemplate,
                sYBLInterraction.loadMetricCompositionRules(serviceTemplate.getMetricCompositonRulesFile()),
                sYBLInterraction.loadJSONEffects(serviceTemplate.getEffectsCompositonRulesFile()));

    }

}
