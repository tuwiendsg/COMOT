package at.ac.tuwien.dsg.comot.graph.model.unit;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.graph.model.type.State;

@NodeEntity
public class NodeInstance implements Serializable {

	private static final long serialVersionUID = 6826825251009392073L;

	@GraphId
	protected Long nodeId;

	protected int instanceId;
	protected int hostedId;
	protected State state;

	public NodeInstance() {

	}

	public NodeInstance(int instanceId, int hostedId, State state) {
		super();
		this.instanceId = instanceId;
		this.hostedId = hostedId;
		this.state = state;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getHostedId() {
		return hostedId;
	}

	public void setHostedId(int hostedId) {
		this.hostedId = hostedId;
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

}
