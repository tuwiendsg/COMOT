package at.ac.tuwien.dsg.comot.common.model.unit;

import at.ac.tuwien.dsg.comot.common.model.type.State;

public class NodeInstance {

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
