package at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.ServiceEntity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.Links.HasResource;

@NodeEntity
@TypeAlias("ResourceType")
public class ResourceType extends ServiceEntity {
	private static final long serialVersionUID = -8806007497355094191L;

	public ResourceType(String name){
		super.setName(name);
	}
	
	public ResourceType(){}
	
//	@RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_RESOURCE, direction=Direction.INCOMING)
//    @Fetch private Set<HasResource> resourceProperties;
}
