package at.ac.tuwien.dsg.comot.samples;

import at.ac.tuwien.dsg.comot.common.model.*;

import static at.ac.tuwien.dsg.comot.bundles.dataends.ElasticSearchNode.ElasticSearchNode;
import static at.ac.tuwien.dsg.comot.common.model.CloudApplication.CloudApplication;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.CostConstraint;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.LatencyConstraint;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTemplate.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

/**
 * @author omoser
 */
public class ElasticSearchCloudApplication {

    public static CloudApplication build() {

        // ElasticSearch Node
        ServiceUnit elasticSearchNode = ElasticSearchNode("ES1")
                .withName("ElasticSearch node (single instance)")
                .constrainedBy(LatencyConstraint("Co1").lessThan("0.5"));

        // OS Node for ES deployment
        OperatingSystemUnit operatingSystemNode = OperatingSystemUnit("OS")
                .providedBy(
                        OpenstackSmall("OS_Headnode_Small")
                                .withProvider("dsg@openstack")
                                .addSoftwarePackage("openjdk-7-jre")
                );

        ServiceTopology searchTopology = ServiceTopology("SearchTopology")
                .consistsOf(elasticSearchNode, operatingSystemNode);

        // Build containing DaaS service
        ServiceTemplate esService = ServiceTemplate("EsService")
                .constrainedBy(CostConstraint("CG0").lessThan("1000"))
                .consistsOf(searchTopology)
                .with(
                        HostedOnRelation("es2os")
                                .from(elasticSearchNode)
                                .to(operatingSystemNode)
                );

        return CloudApplication("EsApp").withName("Simple ElasticSearch Application").consistsOfServices(esService);
    }


}
