package at.ac.tuwien.dsg.comot.orchestrator;

import at.ac.tuwien.dsg.comot.common.model.*;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;

import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.*;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.UnboundedSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;

/**
 * @author omoser
 */
public class DataAsAServiceCloudApplication {

    public static CloudService build() {

        //
        // Cassandra Head Node
        //
        ServiceUnit cassandraHeadNode = SingleSoftwareUnit("CassandraHead")
                .withName("Cassandra head node (single instance)")
                .exposes(Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP"))
                .deployedBy(
                        SingleScriptArtifact(
                                "deployCassandraHead",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraHead.sh")
                )
                .constrainedBy(LatencyConstraint("Co1").lessThan("0.5"));

        //
        // Cassandra Data Node
        //
        ElasticityCapability scaleInCapability = ElasticityCapability.ScaleIn();
        ServiceUnit cassandraDataNode = UnboundedSoftwareUnit("CassandraNode")
                .withName("Cassandra data node (multiple instances)")
                .deployedBy(
                        SingleScriptArtifact
        (
                                "deployCassandraNode",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraNode.sh")
                )
                .requires(Requirement.Variable("CassandraHeadIP_req").withName("Connect to data controller"))
                .constrainedBy(CpuUsageConstraint("Co3").lessThan("50"))
                .provides(scaleInCapability)
                .controlledBy(
                        Strategy("St2")
                        .when(ResponseTimeConstraint("St2Co1").lessThan("300"))
                        .and(ThroughputConstraint("St2Co2").lessThan("400"))
                        .enforce(scaleInCapability)
                );

        //
        // OS Head Node
        //
        OperatingSystemUnit cassandraHeadOsNode = OperatingSystemUnit("OS_Headnode")
                .providedBy(
                        OpenstackSmall()
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                );

        //
        // OS Data Node
        //
        OperatingSystemUnit cassandraDataOsNode = OperatingSystemUnit("OS_Datanode")
                .providedBy(
                        OpenstackSmall()
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                );

        ServiceTopology serviceTopologyConcept = ServiceTopology.ServiceTopology("DataEndTopology");

        serviceTopologyConcept
                .withServiceUnits(cassandraHeadNode,
                        cassandraDataNode,
                        cassandraDataOsNode,
                        cassandraHeadOsNode);
        //
        // Build containing DaaS service
        //
        CloudService daaSService = CloudService.ServiceTemplate("DaasService")
                .constrainedBy(CostConstraint("CG0").lessThan("1000"))
                .andRelationships(
                        ConnectToRelation("head2datanode")
                        .from(cassandraHeadNode.getContext().get("CassandraHeadIP_capa"))
                        .to(cassandraDataNode.getContext().get("CassandraHeadIP_req")),
                        HostedOnRelation("data2os")
                        .from(cassandraHeadNode)
                        .to(cassandraHeadOsNode),
                        HostedOnRelation("controller2os")
                        .from(cassandraDataNode)
                        .to(cassandraDataOsNode)
                );

        return daaSService.withDefaultMetrics();
    }
}
