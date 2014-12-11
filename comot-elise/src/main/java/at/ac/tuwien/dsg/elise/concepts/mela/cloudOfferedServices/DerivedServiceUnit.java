package at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices;

import org.springframework.data.neo4j.annotation.RelationshipEntity;

import at.ac.tuwien.dsg.elise.concepts.Link;
import at.ac.tuwien.dsg.elise.concepts.LinkType;
import at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure.ServiceUnit;

@RelationshipEntity(type = LinkType.CLOUD_OFFER_SERVICE_DERIVES_SERVICE_UNIT)
public class DerivedServiceUnit extends Link {
	private static final long serialVersionUID = -5323859596136838088L;
	String test;
	
	public DerivedServiceUnit(){
		
	}
	
	public DerivedServiceUnit(CloudOfferedServiceUnit source, ServiceUnit target) {		
		super(source,target);
	}
}
