package at.ac.tuwien.dsg.comot.m.core.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class Recording extends Processor {

	public static final String CHANGE_TYPE_LIFECYCLE = "CHANGE_TYPE_LIFECYCLE";
	public static final String CHANGE_TYPE_CUSTOM = "CHANGE_TYPE_CUSTOM";
	public static final String CHANGE_TYPE_EXCEPTION = "CHANGE_TYPE_EXCEPTION";

	public static final String PROP_ORIGIN = "origin";
	public static final String PROP_MSG = "msg";
	public static final String PROP_TARGET = "target";
	public static final String PROP_EVENT_NAME = "eventName";
	public static final String PROP_EXCEPTION = "exception";
	public static final String PROP_EXCEPTION_MSG = "exceptionMsg";

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected RevisionApi revisionApi;

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {
		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName, "#"));
		bindings.add(bindingCustom(queueName, "#"));
		bindings.add(bindingException(queueName, "#"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String originId, CloudService service, Map<String, Transition> transitions)
			throws JAXBException, IllegalArgumentException, IllegalAccessException {

		LifeCycleEvent event = (LifeCycleEvent) msg.getEvent();

		Map<String, String> changeProperties = new HashMap<>();
		changeProperties.put(PROP_ORIGIN, originId);
		changeProperties.put(PROP_TARGET, groupId);
		changeProperties.put(PROP_EVENT_NAME, action.toString());

		// log.info(logId() + "onMessage {}", Utils.asJsonString(msg));

		revisionApi.createOrUpdateRegion(service, instanceId, CHANGE_TYPE_LIFECYCLE, changeProperties);

	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			String event, String epsId, String originId, String optionalMessage) {

		CustomEvent eventMsg = (CustomEvent) msg.getEvent();

		Map<String, String> changeProperties = new HashMap<>();
		changeProperties.put(PROP_ORIGIN, originId);
		changeProperties.put(PROP_TARGET, groupId);
		changeProperties.put(PROP_EVENT_NAME, event);
		if (eventMsg.getMessage() != null) {
			changeProperties.put(PROP_MSG, eventMsg.getMessage());
		}

		if (revisionApi.verifyObject(instanceId, serviceId)) {
			revisionApi.storeEvent(instanceId, CHANGE_TYPE_CUSTOM, changeProperties);
		} else {
			log.error("Custom event happened, but no managed region. {}", msg.getEvent());
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId,
			Exception e) throws Exception {
		/*
		 * Map<String, String> changeProperties = new HashMap<>(); changeProperties.put(PROP_ORIGIN, originId);
		 * changeProperties.put(PROP_EXCEPTION, e.getClass().getName()); changeProperties.put(PROP_EXCEPTION_MSG,
		 * e.getMessage());
		 * 
		 * if (revisionApi.verifyObject(instanceId, serviceId)) { revisionApi.storeEvent(instanceId,
		 * CHANGE_TYPE_EXCEPTION, changeProperties); } else {
		 * log.error("Exception event happened, but no managed region. {}", msg); }
		 */
	}

}
