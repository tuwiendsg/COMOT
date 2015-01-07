package at.ac.tuwien.dsg.comot.recorder.model;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = Change.REL)
public class Change implements Serializable {

	public static final String REL = "CHANGE";

	private static final long serialVersionUID = -4184717754543656669L;

	@GraphId
	protected Long graphId;

	protected Long timestamp;
	protected String type;

	@StartNode
	protected Revision from;
	@EndNode
	protected Revision to;

	public Change() {
	}

	public Change(String type) {
		this();
		this.type = type;
	}

	public Change(Long timestamp, String type, Revision from, Revision to) {
		super();
		this.timestamp = timestamp;
		this.type = type;
		this.from = from;
		this.to = to;
	}

	// GENERATED METHODS

	public Long getGraphId() {
		return graphId;
	}

	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Revision getFrom() {
		return from;
	}

	public void setFrom(Revision from) {
		this.from = from;
	}

	public Revision getTo() {
		return to;
	}

	public void setTo(Revision to) {
		this.to = to;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
