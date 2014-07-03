/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.orchestrator.interraction;

import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import static at.ac.tuwien.dsg.comot.common.model.CloudApplication.CloudApplication;
import at.ac.tuwien.dsg.comot.common.model.ServiceTemplate;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.orchestrator.interraction.rSYBL.rSYBLInterraction;
import at.ac.tuwien.dsg.orchestrator.interraction.salsa.SalsaInterraction;
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

    public void deployAndControl(ServiceTemplate serviceTemplate) {

        CloudApplication application = CloudApplication(serviceTemplate.getId()).withName(serviceTemplate.getId()).consistsOfServices(serviceTemplate).withDefaultMetricsEnabled(true);

        defaultSalsaClient.deploy(application);
        salsaInterraction.waitUntilRunning(serviceTemplate.getId());
        
        try {
            //wait 30 seconds more
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            Logger.getLogger(COMOTOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
        }

        DeploymentDescription deploymentDescription = salsaInterraction.getServiceDeploymentInfo(serviceTemplate.getId());

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