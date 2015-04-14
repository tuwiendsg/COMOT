package at.ac.tuwien.dsg.comot.m.common.enums;

import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionPlanEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent;
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

		if (event instanceof ActionPlanEvent) {
			return rsyblActionPlan(event.getStage());
		} else if (event instanceof ActionEvent) {
			return rsyblAction(event.getStage());
		} else {
			return rsyblCustom(event.getType());
		}
	}

	public static String rsyblActionPlan(IEvent.Stage stage) {

		String eventName = RSYBL_PREFIX;
		eventName += SEPARATOR + ActionPlanEvent.class.getSimpleName().toUpperCase();
		eventName += SEPARATOR + stage;
		return eventName;
	}

	public static String rsyblAction(IEvent.Stage stage) {

		String eventName = RSYBL_PREFIX;
		eventName += SEPARATOR + ActionEvent.class.getSimpleName().toUpperCase();
		eventName += SEPARATOR + stage;
		return eventName;
	}

	public static String rsyblCustom(IEvent.Type type) {
		return rsyblCustomPrefix() + SEPARATOR + type;
	}

	public static String rsyblCustomPrefix() {
		return RSYBL_PREFIX + SEPARATOR + CustomEvent.class.getSimpleName().toUpperCase();
	}

}
