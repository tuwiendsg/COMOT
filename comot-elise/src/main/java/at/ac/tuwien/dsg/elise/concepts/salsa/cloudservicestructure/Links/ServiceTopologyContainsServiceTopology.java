package at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure.Links;

import org.springframework.data.neo4j.annotation.RelationshipEntity;

import at.ac.tuwien.dsg.elise.concepts.Link;
import at.ac.tuwien.dsg.elise.concepts.LinkType;

@RelationshipEntity(type=LinkType.SERVICE_TOPOLOGY_CONTAIN_SERVICE_TOPOLOGY)
public class ServiceTopologyContainsServiceTopology extends Link{
	private static final long serialVersionUID = -8916144361372027769L;

}
