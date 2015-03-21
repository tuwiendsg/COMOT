package at.ac.tuwien.dsg.comot.m.common.coreservices;

import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;

public interface ControlEventsListener {

	public void onMessage(IEvent event);

}
