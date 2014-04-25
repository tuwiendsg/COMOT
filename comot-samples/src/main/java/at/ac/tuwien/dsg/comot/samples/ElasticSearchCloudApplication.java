package at.ac.tuwien.dsg.comot.samples;

import at.ac.tuwien.dsg.comot.bundles.nodes.ElasticSearchNode;
import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemNode;
import at.ac.tuwien.dsg.comot.common.model.ServiceNode;
import at.ac.tuwien.dsg.comot.common.model.ServiceTemplate;

import static at.ac.tuwien.dsg.comot.common.model.CloudApplication.CloudApplication;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.CostConstraint;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.LatencyConstraint;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemNode.OperatingSystemNode;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTemplate.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

/**
 * @author omoser
 */
public class ElasticSearchCloudApplication {

    public static CloudApplication build() {

        // ElasticSearch Node
        ServiceNode elasticSearchNode = ElasticSearchNode.ElasticSearchNode("ES1")
                .withName("ElasticSearch node (single instance)")
                .constrainedBy(LatencyConstraint("Co1").lessThan("0.5"));


        // OS Node for ES deployment
        OperatingSystemNode operatingSystemNode = OperatingSystemNode("OS")
                .providedBy(
                        OpenstackSmall("OS_Headnode_Small")
                                .withProvider("dsg@openstack")
                                .addSoftwarePackage("openjdk-7-jre")
                );

        // Build containing DaaS service
        ServiceTemplate esService = ServiceTemplate("EsService")
                .constrainedBy(CostConstraint("CG0").lessThan("1000"))
                .definedBy(ServiceTopology("EsTopology")
                                .consistsOfNodes(
                                        elasticSearchNode,
                                        operatingSystemNode)
                                .andRelationships(
                                        HostedOnRelation("es2os")
                                                .from(elasticSearchNode)
                                                .to(operatingSystemNode)
                                )
                );

        return CloudApplication("EsApp").withName("Simple ElasticSearch Application").consistsOfServices(esService);
    }
}
