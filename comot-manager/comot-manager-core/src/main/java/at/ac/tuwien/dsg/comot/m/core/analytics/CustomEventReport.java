package at.ac.tuwien.dsg.comot.m.core.analytics;

import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent.Type;

public class CustomEventReport {

	Type type;
	String target;
	String message;
	Long timestamp;

	public CustomEventReport() {

	}

	public CustomEventReport(CustomEvent event, Long timestamp) {
		this.type = event.getType();
		this.target = event.getTarget();
		this.message = event.getMessage();
		this.timestamp = timestamp;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
