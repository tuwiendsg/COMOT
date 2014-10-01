package at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;

@NodeEntity
public class ResourceType extends Entity {
	public ResourceType(String name){
		super.setName(name);
	}
	
	public ResourceType(){}
	
	@RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_RESOURCE, direction=Direction.INCOMING)
    @Fetch private Set<ResourceValue> resourceProperties;
}
