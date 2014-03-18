package at.ac.tuwien.dsg.comot;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.model.*;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.path.xml.element.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static at.ac.tuwien.dsg.comot.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.model.CloudApplication.CloudApplication;
import static at.ac.tuwien.dsg.comot.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.model.Constraint.*;
import static at.ac.tuwien.dsg.comot.model.Constraint.Operator.LessThan;
import static at.ac.tuwien.dsg.comot.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.model.OperatingSystemNode.*;
import static at.ac.tuwien.dsg.comot.model.ServiceTemplate.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.model.Strategy.Action;
import static at.ac.tuwien.dsg.comot.model.Strategy.Strategy;
import static org.junit.Assert.assertEquals;

/**
 * @author omoser
 */
@ContextConfiguration(classes = {ComotContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ModelTest {

    @Autowired
    ToscaDescriptionBuilder builder;

    static volatile String cloudApplicationXmlModel;

    @Before
    public void setupModel() throws Exception {
        if (cloudApplicationXmlModel == null) {
            synchronized (this) {
                if (cloudApplicationXmlModel == null) {
                    cloudApplicationXmlModel = builder.toXml(buildCloudApplication());
                }
            }
        }
    }

    @Test
    public void checkCassandraNode() throws Exception {
        System.out.println(cloudApplicationXmlModel);
        XmlPath xmlPath = XmlPath.from(cloudApplicationXmlModel);
        String nodeRoot = "Definitions.ServiceTemplate.TopologyTemplate.NodeTemplate.findAll{ it.@id == 'CassandraNode'}";
        Node nodeTemplate = xmlPath.get(nodeRoot);
        assertEquals(NodeType.Software.toString(), nodeTemplate.getAttribute("type"));
        assertEquals("CassandraNode", nodeTemplate.getAttribute("id"));
        assertEquals("1", nodeTemplate.getAttribute("minInstances"));
        assertEquals(String.valueOf(Integer.MAX_VALUE), nodeTemplate.getAttribute("maxInstances"));

        Node nodeRequirements = xmlPath.setRoot(nodeRoot).get("Requirements[0]");
        Node nodeRequirement = nodeRequirements.get("Requirement");
        assertEquals("CassandraHeadIP_req", nodeRequirement.getAttribute("id"));
        assertEquals("Connect to data controller", nodeRequirement.getAttribute("name"));
        assertEquals(Requirement.RequirementType.Variable.toString(), nodeRequirement.getAttribute("type"));

        Node nodeDeploymentArtifacts = xmlPath.setRoot(nodeRoot).get("DeploymentArtifacts[0]");
        Node nodeDeploymentArtifact = nodeDeploymentArtifacts.get("DeploymentArtifact");
        assertEquals("deployCassandraNode", nodeDeploymentArtifact.getAttribute("name"));
        assertEquals(ArtifactTemplate.ArtifactType.Script.toString(), nodeDeploymentArtifact.getAttribute("artifactType"));
    }

    @Test
    public void checkCassandraHead() throws Exception {
        System.out.println(cloudApplicationXmlModel);
        XmlPath xmlPath = XmlPath.from(cloudApplicationXmlModel);
        String nodeRoot = "Definitions.ServiceTemplate.TopologyTemplate.NodeTemplate.findAll{ it.@id == 'CassandraHead'}";
        Node nodeTemplate = xmlPath.get(nodeRoot);
        assertEquals(NodeType.Software.toString(), nodeTemplate.getAttribute("type"));
        assertEquals("CassandraHead", nodeTemplate.getAttribute("id"));
        assertEquals("1", nodeTemplate.getAttribute("minInstances"));
        assertEquals("1", nodeTemplate.getAttribute("maxInstances"));

        Node nodeDeploymentArtifacts = xmlPath.setRoot(nodeRoot).get("DeploymentArtifacts[0]");
        Node nodeDeploymentArtifact = nodeDeploymentArtifacts.get("DeploymentArtifact");
        assertEquals("deployCassandraHead", nodeDeploymentArtifact.getAttribute("name"));
        assertEquals(ArtifactTemplate.ArtifactType.Script.toString(), nodeDeploymentArtifact.getAttribute("artifactType"));

        Node nodeCapabilities = xmlPath.setRoot(nodeRoot).get("Capabilities[0]");
        Node nodeCapability = nodeCapabilities.get("Capability");
        assertEquals("CassandraHeadIP_capa", nodeCapability.getAttribute("id"));
        assertEquals(CapabilityType.Variable.toString(), nodeCapability.getAttribute("type"));
    }


    private CloudApplication buildCloudApplication() {

        //
        // Cassandra Head Node
        //
        ServiceNode cassandraHeadNode = SingleSoftwareNode("CassandraHead")
                .withName("Cassandra head node (single instance)")
                .provides(Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP"))
                .deployedBy(
                        SingleScriptArtifactTemplate(
                                "deployCassandraHead",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraHead.sh")
                )
                .constrainedBy(LatencyConstraint("Co1").lessThan("0.5"));

        //
        // Cassandra Data Node
        //
        ServiceNode cassandraDataNode = UnboundedSoftwareNode("CassandraNode")
                .withName("Cassandra data node (multiple instances)")
                .deployedBy(
                        SingleScriptArtifactTemplate(
                                "deployCassandraNode",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraNode.sh")
                )
                .requires(Requirement.Variable("CassandraHeadIP_req").withName("Connect to data controller"))
                .constrainedBy(CpuUsageConstraint("Co3").lessThan("50"))
                .controlledBy(
                        Strategy("St2")
                                .when(ResponseTimeConstraint("St2Co1").lessThan("300"))
                                .and(ThroughputConstraint("St2Co2").lessThan("400"))
                                .then(Action.ScaleIn)
                );

        //
        // OS Head Node
        //
        OperatingSystemNode cassandraHeadOsNode = OperatingSystemNode("OS_Headnode")
                .providedBy(
                        OpenstackSmall("OS_Headnode_Small")
                                .withProvider("dsg@openstack")
                                .addSoftwarePackage("openjdk-7-jre")
                );

        //
        // OS Data Node
        //
        OperatingSystemNode cassandraDataOsNode = OperatingSystemNode("OS_Datanode")
                .providedBy(
                        OpenstackSmall("OS_Datanode_Small")
                                .withProvider("dsg@openstack")
                                .addSoftwarePackage("openjdk-7-jre")
                );


        //
        // Build containing DaaS service
        //
        ServiceTemplate daaSService = ServiceTemplate("DaasService")
                .constrainedBy(CostConstraint("CG0").should(LessThan).value("1000"))
                .definedBy(ServiceTopology("DaasTopology")
                                .consistsOfNodes(
                                        cassandraHeadNode,
                                        cassandraDataNode,
                                        cassandraDataOsNode,
                                        cassandraHeadOsNode)
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
                                )
                );

        return CloudApplication("DaaSApp").consistsOfServices(daaSService);
    }


}
