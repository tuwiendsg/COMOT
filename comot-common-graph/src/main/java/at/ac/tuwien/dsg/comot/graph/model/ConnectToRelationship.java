package at.ac.tuwien.dsg.comot.graph.model;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.graph.model.structure.StackNode;

@RelationshipEntity
public class ConnectToRelationship implements Serializable {

	private static final long serialVersionUID = -8802319806655668518L;

	@GraphId
	protected Long graphId;
	protected String variableId;
	protected String variableValue;

	@StartNode
	protected StackNode from;
	@EndNode
	protected StackNode to;

	public ConnectToRelationship() {
	}

	public ConnectToRelationship(String variableId, StackNode from, StackNode to) {
		super();
		this.variableId = variableId;
		this.from = from;
		this.to = to;
	}

	// GENERATED METHODS

	public String getVariableId() {
		return variableId;
	}

	public void setVariableId(String variableId) {
		this.variableId = variableId;
	}

	public String getVariableValue() {
		return variableValue;
	}

	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}

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

}
