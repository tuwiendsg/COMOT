package at.ac.tuwien.dsg.comot.model.structure;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public abstract class ServicePart implements HasUniqueId, Serializable {

	private static final long serialVersionUID = -889982124609754463L;

	@GraphId
	protected Long nodeId;

	@XmlID
	@XmlAttribute
	@BusinessId
	protected String id;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected State state;

	@XmlElementWrapper(name = "Directives")
	@XmlElement(name = "Directive")
	protected Set<SyblDirective> directives = new HashSet<>();

	public ServicePart() {
	}

	public ServicePart(String id) {
		this.id = id;
	}

	public ServicePart(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addDirective(SyblDirective directive) {
		if (directives == null) {
			directives = new HashSet<>();
		}
		directives.add(directive);
	}

	// GENERATED METHODS

	public Set<SyblDirective> getDirectives() {
		return directives;
	}

	public void setDirectives(Set<SyblDirective> directives) {
		this.directives = directives;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}
}
