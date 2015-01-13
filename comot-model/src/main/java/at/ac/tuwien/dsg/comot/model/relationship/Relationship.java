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

import at.ac.tuwien.dsg.comot.model.structure.StackNode;

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
	protected StackNode from;
	@EndNode
	@XmlIDREF
	@XmlAttribute
	protected StackNode to;

	public Relationship() {
	}

	// GENERATED METHODS

	public StackNode getFrom() {
		return from;
	}

	public void setFrom(StackNode from) {
		this.from = from;
	}

	public StackNode getTo() {
		return to;
	}

	public void setTo(StackNode to) {
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
