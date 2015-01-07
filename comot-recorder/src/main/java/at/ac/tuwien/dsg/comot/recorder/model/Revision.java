package at.ac.tuwien.dsg.comot.recorder.model;

import java.util.UUID;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

@NodeEntity
public class Revision {

	@GraphId
	protected Long nodeId;

	protected UUID id;

	@RelatedToVia(direction = Direction.INCOMING)
	protected Change start;
	@RelatedToVia(direction = Direction.OUTGOING)
	protected Change end;

	public Revision() {
		id = UUID.randomUUID();
	}

	public Revision(Revision oldRev, String changeType, Long timestamp) {
		this();
		Change change = new Change(timestamp, changeType, oldRev, this);
		oldRev.setEnd(change);
		this.setStart(change);
	}

	// GENERATED METHODS

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public Change getStart() {
		return start;
	}

	public void setStart(Change start) {
		this.start = start;
	}

	public Change getEnd() {
		return end;
	}

	public void setEnd(Change end) {
		this.end = end;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
