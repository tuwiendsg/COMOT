package at.ac.tuwien.dsg.comot.m.common.event.state;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ExceptionMessageLifeCycle extends ExceptionMessage {

	private static final long serialVersionUID = -601877313539183398L;

	protected AbstractEvent event;

	public ExceptionMessageLifeCycle() {

	}

	public ExceptionMessageLifeCycle(String serviceId, String origin, Long time, String type,
			String message, String details, AbstractEvent event) {
		super(serviceId, origin, time, type, message, details);
		this.event = event;
	}

	public AbstractEvent getEvent() {
		return event;
	}

	public void setEvent(AbstractEvent event) {
		this.event = event;
	}

}
