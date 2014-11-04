package at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;

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
