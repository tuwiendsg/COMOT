package at.ac.tuwien.dsg.comot.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class SyblDirective implements HasUniqueId, Serializable {

	private static final long serialVersionUID = 1938899881721566640L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected String directive;
	@XmlAttribute
	protected DirectiveType type;

	public SyblDirective() {
	}

	public SyblDirective(String id, DirectiveType type, String directive) {
		this.id = id;
		this.directive = directive;
		this.type = type;
	}

	public String getDirective() {
		return directive;
	}

	public void setDirective(String directive) {
		this.directive = directive;
	}

	public DirectiveType getType() {
		return type;
	}

	public void setType(DirectiveType type) {
		this.type = type;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
