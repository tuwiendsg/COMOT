package at.ac.tuwien.dsg.comot.m.ui.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.type.State;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LcState {

	State name;
	boolean initFinal;

	public LcState() {

	}

	public LcState(State name) {
		super();
		this.name = name;
		if (name.equals(State.NONE)) {
			initFinal = true;
		}
	}

	public State getName() {
		return name;
	}

	public void setName(State name) {
		this.name = name;
	}

	public boolean isInitFinal() {
		return initFinal;
	}

	public void setInitFinal(boolean initFinal) {
		this.initFinal = initFinal;
	}

}
