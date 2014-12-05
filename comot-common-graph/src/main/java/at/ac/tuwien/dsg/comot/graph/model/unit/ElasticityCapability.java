package at.ac.tuwien.dsg.comot.graph.model.unit;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ElasticityCapability {

	@GraphId
	protected Long nodeId;

	protected String name;
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

}
