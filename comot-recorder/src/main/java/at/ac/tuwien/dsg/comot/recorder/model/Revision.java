package at.ac.tuwien.dsg.comot.recorder.model;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class Revision implements Serializable {

	private static final long serialVersionUID = -6465792722114872213L;
	public static final String PROP_ID = "id";

	@XmlTransient
	@GraphId
	protected Long nodeId;

	@XmlID
	protected UUID id;
	@XmlTransient
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
		this.setStart(change);
		oldRev.setEnd(change);

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

	@Override
	public String toString() {
		return "Revision [nodeId=" + nodeId + ", start=" + ((start == null) ? null : start.getGraphId())
				+ ", end=" + ((end == null) ? null : end.getGraphId()) + "]";
	}

}
