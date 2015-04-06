package at.ac.tuwien.dsg.comot.m.ui.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Lc {

	private static final Logger log = LoggerFactory.getLogger(Lc.class);

	@XmlElement(name = "nodes")
	List<LcState> states = new ArrayList<>();
	@XmlElement(name = "links")
	List<LcTransition> transitions = new ArrayList<>();

	public Lc() {

	}

	public void addTransition(State state, Action action, State nextState) {
		transitions.add(new LcTransition(action.toString(), stateNr(state), stateNr(nextState)));
	}

	protected int stateNr(State state) {
		for (int i = 0; i < states.size(); i++) {
			if (states.get(i).getName().equals(state)) {
				return i;
			}
		}

		throw new RuntimeException("error");
	}

	public List<LcState> getStates() {
		return states;
	}

	public void setStates(List<LcState> states) {
		this.states = states;
	}

	public List<LcTransition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<LcTransition> transitions) {
		this.transitions = transitions;
	}

}
