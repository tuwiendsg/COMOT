package at.ac.tuwien.dsg.comot.m.core.analytics;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AnaliticsResult {

	protected UnitInstance instance;
	protected List<Change> events;

	public AnaliticsResult() {

	}

	public AnaliticsResult(UnitInstance instance, List<Change> events) {
		super();
		this.instance = instance;
		this.events = events;
	}

	public UnitInstance getInstance() {
		return instance;
	}

	public void setInstance(UnitInstance instance) {
		this.instance = instance;
	}

	public List<Change> getEvents() {
		return events;
	}

	public void setEvents(List<Change> events) {
		this.events = events;
	}

}
