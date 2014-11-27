package at.ac.tuwien.dsg.comot.common.model.unit;

import java.io.Serializable;

import at.ac.tuwien.dsg.comot.common.model.type.State;

public class NodeInstance implements Serializable {

	private static final long serialVersionUID = 6826825251009392073L;

	protected int instanceId;
	protected int hostedId;
	protected State state;

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

}
