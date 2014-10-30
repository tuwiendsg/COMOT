package at.ac.tuwien.dsg.ElasticityInformationService.concepts;

import java.io.Serializable;
import java.net.URISyntaxException;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


@RelationshipEntity(type=LinkType.ENTITY_LINK)
public class Link implements Serializable{
	private static final long serialVersionUID = -5023121915046141976L;
	@GraphId
    public Long id;
    public String name;
   
	@StartNode ServiceEntity source;
	@EndNode ServiceEntity target;
	//protected String type;

	public Link(ServiceEntity source, ServiceEntity target) {
		this.source = source;
		this.target = target;
	}
	
	public Link(){}
	public Link(String name){
		this.name = name;
	}
	
//	public Entity getSource() {
//		return source;
//	}

	public void setSource(ServiceEntity source) {
		this.source = source;
	}

//	public Entity getTarget() {
//		return target;
//	}

	public void setTarget(ServiceEntity target) {
		this.target = target;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
