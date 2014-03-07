package at.ac.tuwien.dsg.comot;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.model.*;
import org.junit.Test;
import org.oasis.tosca.TDefinitions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import java.io.StringWriter;

import static at.ac.tuwien.dsg.comot.model.CloudApplication.CloudApplication;
import static at.ac.tuwien.dsg.comot.model.Constraint.Constraint;
import static at.ac.tuwien.dsg.comot.model.EntityRelationship.EntityRelationship;
import static at.ac.tuwien.dsg.comot.model.ServiceNode.ServiceNode;
import static at.ac.tuwien.dsg.comot.model.ServiceTemplate.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.model.ServiceTopology.ServiceTopology;

/**
 * @author omoser
 */
public class ModelTest {

    @Test
    public void buildCloudService() throws Exception {

        ServiceNode dataEndServiceNode = ServiceNode("DataEndServiceTopologyService")
                .ofType("DataEndServiceTopology")
                .constrainedBy(
                        Constraint("Co1")
                                .forMetric(Constraint.Metric.Latency)
                                .should(Constraint.Operator.LessThan)
                                .value("0.5"),
                        Constraint("Co2")
                                .forMetric(Constraint.Metric.CpuUsage)
                                .should(Constraint.Operator.LessThan)
                                .value("83")
                );

        ServiceNode eventProcessingServiceNode = ServiceNode("EventProcessingServiceTopologyService")
                .ofType("EventProcessingServiceTopology")
                .constrainedBy(
                        Constraint("Co3")
                                .forMetric(Constraint.Metric.ResponseTime)
                                .should(Constraint.Operator.LessThan)
                                .value("350")
                );


        ServiceTemplate demoApp = ServiceTemplate("DemoApp")
                .constrainedBy(
                        Constraint("costConstraint")
                                .forMetric(Constraint.Metric.Cost)
                                .should(Constraint.Operator.LessThan)
                                .value("1000")
                )

                .definedBy(
                        ServiceTopology("DemoTopology")
                                .consistsOfNodes(
                                        dataEndServiceNode,
                                        eventProcessingServiceNode
                                )
                                .andRelationships(
                                        EntityRelationship("directedRelation")
                                                .from(eventProcessingServiceNode)
                                                .to(dataEndServiceNode)
                                                .ofType(EntityRelationship.RelationshipType.ConnectedTo))

                );



        CloudApplication application = CloudApplication("DaasApp")
                .consistsOfServices(demoApp);

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
