package at.ac.tuwien.dsg.comot.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class ElasticityCapability {

	@GraphId
	protected Long nodeId;

	@BusinessId
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected String script;

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
