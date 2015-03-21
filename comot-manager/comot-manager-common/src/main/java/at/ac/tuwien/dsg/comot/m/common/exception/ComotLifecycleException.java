package at.ac.tuwien.dsg.comot.m.common.exception;

import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;

public class ComotLifecycleException extends Exception {

	private static final long serialVersionUID = 6270339245853337512L;

	protected LifeCycleEvent event;

	public ComotLifecycleException(String message, LifeCycleEvent event) {
		super(message);
		this.event = event;
	}

}
