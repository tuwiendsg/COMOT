package at.ac.tuwien.dsg.comot.m.recorder.model;

import java.io.Serializable;
import java.util.Map;
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
	public static final String PROP_ID = "revisionId";

	@XmlTransient
	@GraphId
	protected Long nodeId;

	@XmlID
	protected String revisionId;
	@XmlTransient
	@RelatedToVia(direction = Direction.INCOMING)
	protected Change start;
	@RelatedToVia(direction = Direction.OUTGOING)
	protected Change end;

	public Revision() {
		revisionId = UUID.randomUUID().toString();
	}

	public Revision(Revision oldRev, String changeType, String targetObjectId, Map<String, Object> changeProperties,
			Long timestamp) {
		this();
		Change change = new Change(timestamp, changeType, targetObjectId, changeProperties, oldRev, this);
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

	public String getRevisionId() {
		return revisionId;
	}

	public void setRevisionId(String id) {
		this.revisionId = id;
	}

	@Override
	public String toString() {
		return "Revision [nodeId=" + nodeId + ", start=" + ((start == null) ? null : start.getGraphId())
				+ ", end=" + ((end == null) ? null : end.getGraphId()) + "]";
	}

}
