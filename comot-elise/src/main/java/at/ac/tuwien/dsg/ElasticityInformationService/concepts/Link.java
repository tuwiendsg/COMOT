package at.ac.tuwien.dsg.ElasticityInformationService.concepts;

import java.io.Serializable;
import java.net.URISyntaxException;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


@RelationshipEntity
public class Link implements Serializable{
    @GraphId
    public Long id;
    public String name;
   
	@StartNode Entity source;
	@EndNode Entity target;
	//protected String type;

	public Link(Entity source, Entity target) throws URISyntaxException {
		this.source = source;
		this.target = target;
	}
	
	public Link(){}
	public Link(String name){
		this.name = name;
	}

	public Entity getSource() {
		return source;
	}

	public void setSource(Entity source) {
		this.source = source;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
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
