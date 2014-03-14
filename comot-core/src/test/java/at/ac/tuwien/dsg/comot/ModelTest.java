package at.ac.tuwien.dsg.comot;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.model.*;
import org.junit.Test;
import org.oasis.tosca.TDefinitions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

import static at.ac.tuwien.dsg.comot.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.model.CloudApplication.CloudApplication;
import static at.ac.tuwien.dsg.comot.model.Constraint.*;
import static at.ac.tuwien.dsg.comot.model.Constraint.Operator.LessThan;
import static at.ac.tuwien.dsg.comot.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.model.ServiceNode.*;
import static at.ac.tuwien.dsg.comot.model.ServiceTemplate.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.model.Strategy.Action;
import static at.ac.tuwien.dsg.comot.model.Strategy.Strategy;

/**
 * @author omoser
 */
public class ModelTest {

    @Test
    public void buildCloudService() throws Exception {


/*
                                Constraint("Co2")
                                        .forMetric(Constraint.Metric.CpuUsage)
                                        .should(Constraint.Operator.LessThan)
                                        .value("83")
*/


        //
        // Cassandra Head Node
        //
        ServiceNode cassandraHeadNode = SingleSoftwareNode("CassandraHead")
                .withName("Cassandra head node (single instance)")

                .provides(
                        Capability.Variable("CassandraHeadIP_capa").withName("Data controller IP")
                )

                .deployedBy(
                        SingleScriptArtifactTemplate(
                                "deployCassandraHead",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraHead.sh"
                        )
                )

                .constrainedBy(
                        LatencyConstraint("Co1").should(LessThan).value("0.5")
                );

        //
        // Cassandra Data Node
        //
        ServiceNode cassandraDataNode = UnboundedSoftwareNode("CassandraNode")
                .withName("Cassandra data node (multiple instances)")

                .deployedBy(
                        SingleScriptArtifactTemplate(
                                "deployCassandraNode",
                                "http://134.158.75.65/salsa/upload/files/daas/deployCassandraNode.sh"
                        )
                )

                .requires(
                        Requirement.Variable("CassandraHeadIP_req").withName("Connect to data controller")

                )

                .constrainedBy(
                        CpuUsageConstraint("Co3").should(LessThan).value("50")
                )

                .controlledBy(
                        Strategy("St2")
                                .when(ResponseTimeConstraint("St2Co1").should(LessThan).value("300"))
                                .and(ThroughputConstraint("St2Co2").should(LessThan).value("400"))
                                .then(Action.ScaleIn)
                );

        //
        // OS Head Node
        //
        ServiceNode cassandraHeadOsNode = OperatingSystemNode("OS_Headnode");


        ServiceTemplate daaSService = ServiceTemplate("DaasService")
                .constrainedBy(
                        CostConstraint("CG0").should(LessThan).value("1000")
                )

                .definedBy(
                        ServiceTopology("DaasTopology")

                                .consistsOfNodes(
                                        cassandraHeadNode,
                                        cassandraDataNode
                                )

                               .andRelationships(
                                       ConnectToRelation("head2datanode")
                                               .from(cassandraHeadNode.getContext().get("CassandraHeadIP_capa"))
                                               .to(cassandraDataNode.getContext().get("CassandraHeadIP_req"))
                                               .ofType(EntityRelationship.RelationshipType.ConnectedTo)
                               )
                );


        CloudApplication application = CloudApplication("DaaSApp")
                .consistsOfServices(daaSService);


        TDefinitions tDefinitions = new ToscaDescriptionBuilderImpl().buildToscaDefinitions(application);
        JAXBContext jaxbContext = JAXBContext.newInstance(TDefinitions.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        marshaller.marshal(tDefinitions, writer);
        System.out.println(writer.toString());
    }
}
