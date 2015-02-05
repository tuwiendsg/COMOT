package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.ServiceArtifact;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
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
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.UnboundedSoftwareUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ProgrammingAndControllingElasticityWithCOMOTSensors {

    public static void main(String[] args) {
        //specify service units in terms of software

        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit dataControllerVM = OperatingSystemUnit("SensorVM")
                .providedBy(OpenstackSmall()
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        //start with Data End, and first with Data Controller
        ServiceUnit dataControllerUnit = SingleSoftwareUnit("sensor_gas")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(ServiceArtifact("http://128.130.172.215/salsa/upload/files/DaasService/IoT/run_sensor_gas.sh")) //data controller exposed its IP 
                ;

        ServiceTopology sensors = ServiceTopology("sensors").withServiceUnits(dataControllerUnit);

        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("sensors")
                .consistsOfTopologies(sensors)
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among VMs
                .withDefaultMetrics()
                //to find scaling actions, one must assume some effects for each action, to understand
                //if it makes sense or not to execute the action
                .withDefaultActionEffects();

        //instantiate COMOT orchestrator to deploy, monitor and control the service
        COMOTOrchestrator orchestrator = new COMOTOrchestrator()
                //we have SALSA as cloud management tool
                //curently deployed separately
 
 
                .withSalsaIP("localhost")
                .withSalsaPort(8080)
 
                //we have rSYBL elasticity control service and MELA 
                //deployed separately
                .withRsyblIP("localhost")
                //                .withRsyblIP("localhost")
                //                .withRsyblIP("localhost")
                .withRsyblPort(8080);

        //deploy, monitor and control
//        orchestrator.deployAndControl(serviceTemplate);
//        orchestrator.deploy(serviceTemplate);
        orchestrator.controlExisting(serviceTemplate);

    }
}
