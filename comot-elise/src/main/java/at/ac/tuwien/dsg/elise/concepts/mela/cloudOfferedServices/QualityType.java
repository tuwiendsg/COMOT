package at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.elise.concepts.LinkType;
import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.Links.HasQuality;

@NodeEntity
@TypeAlias("QualityType")
public class QualityType extends ServiceEntity {
	public QualityType(String name){
		super.setName(name);
	}
	
	public QualityType(){}
	
//	@RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_QUALITY, direction=Direction.INCOMING)
//    @Fetch private Set<HasQuality> qualityProperties;
}
