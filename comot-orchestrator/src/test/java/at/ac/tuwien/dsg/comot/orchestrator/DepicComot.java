/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author Jun
 */

import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.WarArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.LocalDocker;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.LifecyclePhase;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

public class DepicComot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        deployAndControlElasticDaaS();
    }
    
    private static void deployAndControlElasticDaaS(){
        
        ServiceTopology monitoringServicesTopology = ServiceTopology("Monitoring_Services_Topology");
  
        String artifactRepo = "http://128.130.172.215/salsa/upload/files/jun/artifact_sh/";
 
        CloudService cloudService = ServiceTemplate("EDaaSCloudService")
                .consistsOfTopologies(monitoringServicesTopology)
                .withDefaultMetrics();
            

        ElasticityCapability eventProcessingUnitScaleIn = ElasticityCapability.ScaleIn().withPrimitiveOperations("scalein");
        ElasticityCapability eventProcessingUnitScaleOut = ElasticityCapability.ScaleOut().withPrimitiveOperations("scaleout" );
       
        // add VM + monitoring service units
        OperatingSystemUnit monitoringVM = OperatingSystemUnit("MonitoringServices_VM")
                .providedBy(OpenstackSmall()
                        .addSoftwarePackage("tomcat7")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );
        monitoringServicesTopology.addServiceUnit(monitoringVM);

        String actionID = "datacompletenessMeasurement";
        int i=0;
        
        ServiceUnit serviceUnit = SingleSoftwareUnit(actionID + "_SU")
                .deployedBy(SingleScriptArtifact(actionID + "Artifact", artifactRepo + actionID + ".sh"))
                .controlledBy(Strategy("EP_ST1_" + (i++))
                        .when(Constraint.MetricConstraint("EP_ST1_CO1_" + (i++), new Constraint.Metric("cpuUsage", "%")).lessThan("5"))
                        .enforce(eventProcessingUnitScaleIn),
                        Strategy("EP_ST2_" + (i++))
                        .when(Constraint.MetricConstraint("EP_ST1_CO2_" + (i++), new Constraint.Metric("cpuUsage", "%")).greaterThan("6"))
                        .enforce(eventProcessingUnitScaleOut));

        monitoringServicesTopology.addServiceUnit(serviceUnit);
        cloudService.andRelationships(HostedOnRelation(serviceUnit.getId() + "To" + monitoringVM.getId())
                .from(serviceUnit)
                .to(monitoringVM));



         COMOTOrchestrator orchestrator = new COMOTOrchestrator()
                //we have SALSA as cloud management tool
                //curently deployed separately
 
                .withSalsaIP("128.130.172.216")
                .withSalsaPort(8380)
                .withRsyblIP("128.130.172.216")
                .withRsyblPort(8280);
         
        //deploy, monitor and control
        orchestrator.deployAndControl(cloudService);
        
  
        
    }
    
}
