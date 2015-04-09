package at.ac.tuwien.dsg.comot.m.common.enums;

import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;

public enum ComotEvent {

	// /**
	// * an instance is waiting for other condition before deployment. E.g, VM is creating by cloud, software is waiting
	// * for VM or waiting for other "connect to"
	// */
	// ALLOCATING,
	// /**
	// * the deployment command is assigned and waiting pioneer to get that command.
	// */
	// STAGING,
	// /**
	// * VM is initiating, e.g. setting up pioneer, install predefined packages; or software artifact is downloading,
	// * creating workspace, etc.
	// */
	// CONFIGURING,
	// /**
	// * running configuration script
	// */
	// INSTALLING,
	// /**
	// * the same with STAGING, but for custom configuration action at runtime
	// */
	// STAGING_ACTION,

	MELA_START,
	MELA_STOP,
	MELA_SET_MCR,
	MELA_GET_MCR,
	MELA_GET_STRUCTURE,

	RSYBL_START,
	RSYBL_STOP,
	RSYBL_SET_MCR,
	RSYBL_SET_EFFECTS;

	public static final String RSYBL_PREFIX = "RSYBL";
	public static final String SEPARATOR = "-";

	public static String rsyblEventName(IEvent event) {

		IEvent.Type type = event.getType();
		IEvent.Stage stage = event.getStage();
		String eventName = RSYBL_PREFIX + SEPARATOR + event.getClass().getSimpleName().toUpperCase();

		if (type != null) {
			eventName += SEPARATOR + type;
		}

		if (stage != null) {
			eventName += SEPARATOR + stage;
		}
		return eventName;
	}
}
