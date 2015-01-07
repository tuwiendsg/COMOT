package at.ac.tuwien.dsg.comot.graph.model.node;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.graph.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.graph.model.type.State;

@NodeEntity
public class NodeInstance implements Serializable {

	private static final long serialVersionUID = 6826825251009392073L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	protected String id;

	protected int instanceId;
	protected State state;
	protected NodeInstance hostInstance;

	public NodeInstance() {

	}

	public NodeInstance(StackNode node, int instanceId, State state, NodeInstance hostInstance) {
		super();
		this.instanceId = instanceId;
		this.state = state;
		this.hostInstance = hostInstance;
		id = node.getId() + "_" + instanceId;
	}

	// GENERATED METHODS

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public NodeInstance getHostInstance() {
		return hostInstance;
	}

	public void setHostInstance(NodeInstance hostInstance) {
		this.hostInstance = hostInstance;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
