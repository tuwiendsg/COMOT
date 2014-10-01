package at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;

@NodeEntity
public class QualityType extends Entity {
	public QualityType(String name){
		super.setName(name);
	}
	
	public QualityType(){}
	
	@RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_QUALITY, direction=Direction.INCOMING)
    @Fetch private Set<QualityValue> qualityProperties;
}
