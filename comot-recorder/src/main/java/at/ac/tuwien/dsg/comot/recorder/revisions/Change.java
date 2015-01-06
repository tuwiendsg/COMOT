package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = "CHANGE")
public class Change implements Serializable {

	private static final long serialVersionUID = -4184717754543656669L;

	@GraphId
	protected Long graphId;

	protected Long timestampId;
	protected ChangeType type;

	@StartNode
	protected Revision from;
	@EndNode
	protected Revision to;

	public enum ChangeType {
		SCALE_OUT, SCALE_IN, CONFIG_UPDATE
	}

	public Change() {
		timestampId = System.currentTimeMillis();
	}

	public Change(ChangeType type) {
		this();
		this.type = type;
	}

	// GENERATED METHODS

	public Long getGraphId() {
		return graphId;
	}

	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	public ChangeType getType() {
		return type;
	}

	public void setType(ChangeType type) {
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

	public Long getTimestampId() {
		return timestampId;
	}

	public void setTimestampId(Long timestampId) {
		this.timestampId = timestampId;
	}

}
