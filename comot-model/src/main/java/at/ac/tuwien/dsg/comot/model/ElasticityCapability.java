package at.ac.tuwien.dsg.comot.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class ElasticityCapability implements Serializable {

	private static final long serialVersionUID = 7875588761489314170L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected String script;

	public ElasticityCapability() {

	}

	public ElasticityCapability(String name, String script) {
		super();
		this.name = name;
		this.script = script;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
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
