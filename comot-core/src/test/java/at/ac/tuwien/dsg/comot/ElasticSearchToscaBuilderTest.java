package at.ac.tuwien.dsg.comot;

import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
import at.ac.tuwien.dsg.comot.core.ComotContext;
import at.ac.tuwien.dsg.comot.core.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.core.test.samples.ElasticSearchCloudApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author omoser
 */
@ContextConfiguration(classes = {ComotContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ElasticSearchToscaBuilderTest {

    @Autowired
    ToscaDescriptionBuilder builder;

    static String CloudServiceXmlModel;

    @Test
    public void setupModel() throws Exception {
       JsonBundleLoader.getInstance().init();
        if (CloudServiceXmlModel == null) {
            CloudServiceXmlModel = builder.toXml(ElasticSearchCloudApplication.build());
            System.out.println("Using the following cloud application for tests:  " + CloudServiceXmlModel);
        }
    }

   /* @Test
    public void checkCassandraNode() throws Exception {
        XmlPath xmlPath = XmlPath.from(CloudServiceXmlModel);
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
        assertEquals(buildExpectedScriptArtifactType(), nodeDeploymentArtifact.getAttribute("artifactType"));
    }

    @Test
    public void checkCassandraHead() throws Exception {
        XmlPath xmlPath = XmlPath.from(CloudServiceXmlModel);
        String nodeRoot = "Definitions.ServiceTemplate.TopologyTemplate.NodeTemplate.findAll{ it.@id == 'CassandraHead'}";
        Node nodeTemplate = xmlPath.get(nodeRoot);
        assertEquals(NodeType.Software.toString(), nodeTemplate.getAttribute("type"));
        assertEquals("CassandraHead", nodeTemplate.getAttribute("id"));
        assertEquals("1", nodeTemplate.getAttribute("minInstances"));
        assertEquals("1", nodeTemplate.getAttribute("maxInstances"));

        Node nodeDeploymentArtifacts = xmlPath.setRoot(nodeRoot).get("DeploymentArtifacts[0]");
        Node nodeDeploymentArtifact = nodeDeploymentArtifacts.get("DeploymentArtifact");
        assertEquals("deployCassandraHead", nodeDeploymentArtifact.getAttribute("name"));
        assertEquals(buildExpectedScriptArtifactType(), nodeDeploymentArtifact.getAttribute("artifactType"));

        Node nodeCapabilities = xmlPath.setRoot(nodeRoot).get("Capabilities[0]");
        Node nodeCapability = nodeCapabilities.get("Capability");
        assertEquals("CassandraHeadIP_capa", nodeCapability.getAttribute("id"));
        assertEquals(CapabilityType.Variable.toString(), nodeCapability.getAttribute("type"));
    }

    private String buildExpectedScriptArtifactType() {
        return ToscaDescriptionBuilderImpl.DEFAULT_ARTIFACT_TYPE_PREFIX  + ":" + ArtifactTemplate.ArtifactType.Script.toString();
    }

    @Test
    public void checkOsHeadNode() {
        checkOsNode("OS_Headnode", "1", "1");
    }

    @Test
    public void checkOsDataNode() {
        checkOsNode("OS_Datanode", "1", "1");
    }

    @Test
    public void checkHead2DataNodeRelationshipTemplate() {
        checkRelationshipTemplate("head2datanode", ConnectedTo.toString(), "CassandraHeadIP_capa", "CassandraHeadIP_req");
    }

    @Test
    public void checkData2OsNodeRelationshipTemplate() {
        checkRelationshipTemplate("data2os", HostedOn.toString(), "CassandraHead", "OS_Headnode");
    }

    @Test
    public void checkController2OsNodeRelationshipTemplate() {
        checkRelationshipTemplate("controller2os", HostedOn.toString(), "CassandraNode", "OS_Datanode");
    }

    private void checkRelationshipTemplate(String templateId, String type, String source, String target) {
        XmlPath xmlPath = XmlPath.from(CloudServiceXmlModel);
        String nodeRoot = "Definitions.ServiceTemplate.TopologyTemplate.RelationshipTemplate.findAll{ it.@id == '" + templateId + "'}";
        Node relationshipTemplate = xmlPath.get(nodeRoot);
        assertNotNull(relationshipTemplate);
        assertEquals(templateId, relationshipTemplate.getAttribute("id"));
        assertEquals(type, relationshipTemplate.getAttribute("type"));

        Node sourceElement = relationshipTemplate.getNode("SourceElement");
        assertEquals(source, sourceElement.getAttribute("ref"));

        Node targetElement = relationshipTemplate.getNode("TargetElement");
        assertEquals(target, targetElement.getAttribute("ref"));
    }


    private void checkOsNode(String nodeId, String minInstances, String maxInstances) {
        XmlPath xmlPath = XmlPath.from(CloudServiceXmlModel);
        String nodeRoot = "Definitions.ServiceTemplate.TopologyTemplate.NodeTemplate.findAll{ it.@id == '" + nodeId + "'}";
        Node nodeTemplate = xmlPath.get(nodeRoot);
        assertEquals(NodeType.OperatingSystem.toString(), nodeTemplate.getAttribute("type"));
        assertEquals(nodeId, nodeTemplate.getAttribute("id"));
        assertEquals(minInstances, nodeTemplate.getAttribute("minInstances"));
        assertEquals(maxInstances, nodeTemplate.getAttribute("maxInstances"));

        Node nodeProperties = xmlPath.setRoot(nodeRoot).get("Properties[0]");
        Node mappingProperties = nodeProperties.get("MappingProperties");
        Node mappingProperty = mappingProperties.get("MappingProperty");
        assertNotNull(mappingProperty);
        assertEquals("os", mappingProperty.getAttribute("type"));
        List<Node> properties = mappingProperty.getNodes("property");
        for (Node property : properties) {
            String name = property.getAttribute("name");
            switch (name) {
                case "instanceType":
                    assertEquals("m1.small", property.value());
                    break;
                case "provider":
                    assertEquals("dsg@openstack", property.value());
                    break;
                case "baseImage":
                    assertEquals("ami-00000163", property.value());
                    break;
                case "packages":
                    assertEquals("openjdk-7-jre", property.value());
                    break;
                default:
                    fail("Unexpected name: " + name);
            }
        }
    }
*/
}

