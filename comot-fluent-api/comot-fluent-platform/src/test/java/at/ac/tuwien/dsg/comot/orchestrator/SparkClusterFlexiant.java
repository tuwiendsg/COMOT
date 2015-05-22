/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
import static at.ac.tuwien.dsg.comot.common.model.MetricEffect.MetricEffect;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.LifecyclePhase;
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jun
 */
public class SparkClusterFlexiant {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        String[] names = new String[]{"SparkClusterDaniel_idle", "SparkClusterDaniel_0", "SparkClusterDaniel_1", "SparkClusterDaniel_2", "SparkClusterDaniel_3", "SparkClusterDaniel_4"};
        String[] ports = new String[]{"ddd","9900", "9901", "9905", "9910", "9950"};

        for( int i = 0; i < names.length; i++)
//        int i = 5;
        {
            createSparkCluster(names[i], ports[i]);
//        try {
//            Thread.sleep(60000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(SparkCluster.class.getName()).log(Level.SEVERE, null, ex);
//        }
        }

    }

    private static void createSparkCluster(String cloudServiceName, String port) {

//        String salsaRepo = "http://128.130.172.215/salsa/upload/files/Spark/";
        String salsaRepo = "http://109.231.121.57/Spark/";

        OperatingSystemUnit sparkMasterVM = OperatingSystemUnit("SparkMasterVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantMicro()
                        .withBaseImage("4ddb13c2-ce8a-36f9-a95f-87f34b1fd64a")
                //                        .addSoftwarePackage("openjdk-7-jre")
                //                        .addSoftwarePackage("ganglia-monitor")
                //                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit sparkWorkerVM = OperatingSystemUnit("SparkWorkerVM1")
                .providedBy(CommonOperatingSystemSpecification.FlexiantMicro()
                        .withBaseImage("4ddb13c2-ce8a-36f9-a95f-87f34b1fd64a")
                //                        .addSoftwarePackage("openjdk-7-jre")
                //                        .addSoftwarePackage("ganglia-monitor")
                //                        .addSoftwarePackage("gmetad")
                );

        ServiceUnit sparkMasterUnit = SingleSoftwareUnit("SparkMasterUnit")
                .deployedBy(ArtifactTemplate.MiscArtifact(salsaRepo + "deploySparkMaster.sh"))
                .exposes(Capability.Variable("SparkMaster_IP_information"))
                .withLifecycleAction(LifecyclePhase.DEPLOY, BASHAction("./deploySparkMaster.sh 109.231.121.57 " + port));

        ServiceUnit sparkWorkerUnit = SingleSoftwareUnit("SparkWorkerUnit")
                .deployedBy(ArtifactTemplate.SingleScriptArtifact(salsaRepo + "deploySparkWorker.sh"))
                .requires(Requirement.Variable("SparkMaster_IP_Req").withName("requiringMasterIP")
                );

        ServiceTopology sparkTopology = ServiceTopology("SparkTopology")
                .withServiceUnits(sparkWorkerUnit, sparkMasterUnit //add also OS units to topology
                        , sparkMasterVM, sparkWorkerVM
                );

        CloudService serviceTemplate = ServiceTemplate(cloudServiceName)
                .consistsOfTopologies(sparkTopology)
                .andRelationships(
                        ConnectToRelation("SparkMasterUnitToSparkWorkerUnit")
                        .from(sparkMasterUnit.getContext().get("SparkMaster_IP_information"))
                        .to(sparkWorkerUnit.getContext().get("SparkMaster_IP_Req")) //specify which software unit goes to which VM
                        ,
                        HostedOnRelation("sparkMasterUnitToVM")
                        .from(sparkMasterUnit)
                        .to(sparkMasterVM),
                        HostedOnRelation("sparkWorker1ToVM")
                        .from(sparkWorkerUnit)
                        .to(sparkWorkerVM)
                )
                .withDefaultMetrics();

        COMOTOrchestrator orchestrator = new COMOTOrchestrator()
                //                .withSalsaIP("128.130.172.215")
                .withSalsaIP("109.231.121.57")
                .withSalsaPort(8380);

        orchestrator.deploy(serviceTemplate);

    }

}
