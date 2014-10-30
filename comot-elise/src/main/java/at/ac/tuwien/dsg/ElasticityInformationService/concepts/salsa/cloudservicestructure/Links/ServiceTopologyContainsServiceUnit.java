package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.Links;

import org.springframework.data.neo4j.annotation.RelationshipEntity;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Link;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;

@RelationshipEntity(type=LinkType.SERVICE_TOPOLOGY_CONTAIN_SERVICE_TOPOLOGY)
public class ServiceTopologyContainsServiceUnit extends Link {
	private static final long serialVersionUID = -916179329653325871L;

}
