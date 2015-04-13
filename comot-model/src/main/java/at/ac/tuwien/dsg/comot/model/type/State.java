package at.ac.tuwien.dsg.comot.model.type;

public enum State {

	INIT,
	PASSIVE,
	DEPLOYING,
	RUNNING,
	ELASTIC_CHANGE,
	MAINTENANCE,
	UNDEPLOYING,
	FINAL,
	ERROR;

	static public boolean isMember(String aName) {
		State[] states = State.values();
		for (State state : states) {
			if (state.name().equals(aName)) {
				return true;
			}
		}
		return false;
	}

}
