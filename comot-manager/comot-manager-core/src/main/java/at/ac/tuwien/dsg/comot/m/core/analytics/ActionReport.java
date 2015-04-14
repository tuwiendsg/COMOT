package at.ac.tuwien.dsg.comot.m.core.analytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionReport {

	String targetId;
	String actionId;
	IEvent.Stage currentStage;
	Long timestamp;
	Set<String> effectedInstances;
	List<CustomEventReport> customEvents;

	public ActionReport() {

	}

	public ActionReport(ActionEvent event, Long timestamp) {
		super();
		this.targetId = event.getTargetId();
		this.actionId = event.getActionId();
		this.timestamp = timestamp;
		currentStage = IEvent.Stage.START;
	}

	public void addEffectedInstances(String id) {

		if (effectedInstances == null) {
			effectedInstances = new HashSet<>();
		}
		effectedInstances.add(id);
	}

	public void addCustomEvent(CustomEvent event, Long timestamp) {

		if (customEvents == null) {
			customEvents = new ArrayList<>();
		}
		customEvents.add(new CustomEventReport(event, timestamp));
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public IEvent.Stage getCurrentStage() {
		return currentStage;
	}

	public void setCurrentStage(IEvent.Stage currentStage) {
		this.currentStage = currentStage;
	}

	public List<CustomEventReport> getCustomEvents() {
		return customEvents;
	}

	public void setCustomEvents(List<CustomEventReport> customEvents) {
		this.customEvents = customEvents;
	}

	public Set<String> getEffectedInstances() {
		return effectedInstances;
	}

	public void setEffectedInstances(Set<String> effectedInstances) {
		this.effectedInstances = effectedInstances;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
