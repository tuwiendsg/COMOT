package at.ac.tuwien.dsg.comot.m.ui.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LcTransition {

	String name;
	int source;
	int target;

	public LcTransition() {

	}

	public LcTransition(String name, int source, int target) {
		super();
		this.name = name;
		this.source = source;
		this.target = target;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

}
