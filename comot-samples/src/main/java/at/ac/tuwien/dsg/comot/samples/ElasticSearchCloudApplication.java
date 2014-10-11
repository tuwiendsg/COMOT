package at.ac.tuwien.dsg.comot.samples;

import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;

import static at.ac.tuwien.dsg.comot.bundles.dataends.ElasticSearchNode.ElasticSearchNode;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.CostConstraint;
import static at.ac.tuwien.dsg.comot.common.model.Constraint.LatencyConstraint;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;

/**
 * @author omoser
 */
public class ElasticSearchCloudApplication {

    public static CloudService build() {

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

        ServiceTopology searchTopology = ServiceTopology.ServiceTopology("SearchTopology");

        searchTopology
                .withServiceUnits(elasticSearchNode,
                        operatingSystemNode);

        // Build containing DaaS service
        CloudService esService = ServiceTemplate("EsService")
                .constrainedBy(CostConstraint("CG0").lessThan("1000"))
                .consistsOfTopologies(searchTopology)
                .andRelationships(
                        HostedOnRelation("es2os")
                        .from(elasticSearchNode)
                        .to(operatingSystemNode)
                );

        return esService;
    }

    public void t() {

        ServiceUnit esNode = ElasticSearchNode("ES1")
                .withName("ElasticSearch node (single instance)")
                .constrainedBy(LatencyConstraint("Co1").lessThan("0.5"));

        OperatingSystemUnit osNode = OperatingSystemUnit("OS")
                .providedBy(
                        OpenstackSmall("OS_Headnode_Small")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                );

        ServiceTopology esTopology = ServiceTopology.ServiceTopology("ESTopology");
        esTopology
                .withServiceUnits(esNode,
                        osNode);

        CloudService esService = ServiceTemplate("EsService")
                .constrainedBy(CostConstraint("CG0").lessThan("1000"))
                .consistsOfTopologies(esTopology)
                .andRelationships(
                        HostedOnRelation("es2os")
                        .from(esNode)
                        .to(osNode)
                );
 
    }
}
