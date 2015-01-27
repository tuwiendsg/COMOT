package at.ac.tuwien.dsg.comot.model.relationship;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;

@XmlAccessorType(XmlAccessType.FIELD)
@RelationshipEntity
public abstract class Relationship implements Serializable {

	private static final long serialVersionUID = 435724025669303243L;

	@GraphId
	protected Long graphId;

	@XmlID
	@XmlAttribute
	protected String id;

	@StartNode
	@XmlIDREF
	@XmlAttribute
	protected ServiceUnit from;
	@EndNode
	@XmlIDREF
	@XmlAttribute
	protected ServiceUnit to;

	public Relationship() {
	}

	// GENERATED METHODS

	public ServiceUnit getFrom() {
		return from;
	}

	public void setFrom(ServiceUnit from) {
		this.from = from;
	}

	public ServiceUnit getTo() {
		return to;
	}

	public void setTo(ServiceUnit to) {
		this.to = to;
	}

	public Long getGraphId() {
		return graphId;
	}

	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
