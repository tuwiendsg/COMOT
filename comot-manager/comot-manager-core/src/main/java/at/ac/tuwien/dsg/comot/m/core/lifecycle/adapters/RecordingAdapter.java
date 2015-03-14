package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class RecordingAdapter extends Adapter {

	public static final String CHANGE_TYPE_LIFECYCLE = "CHANGE_TYPE_LIFECYCLE";
	public static final String CHANGE_TYPE_CUSTOM = "CHANGE_TYPE_CUSTOM";

	public static final String PROP_ORIGIN = "origin";
	public static final String PROP_MSG = "msg";
	public static final String PROP_TARGET = "target";
	public static final String PROP_EVENT_NAME = "eventName";

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected RevisionApi revisionApi;

	protected Binding binding1;
	protected Binding binding2;

	@Override
	public void start(String osuInstanceId) {

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"#", null);
		binding2 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				"#", null);

		admin.declareBinding(binding1);
		admin.declareBinding(binding2);

		container.setMessageListener(new CustomListener(osuInstanceId));

	}

	class CustomListener extends AdapterListener {

		public CustomListener(String adapterId) {
			super(adapterId);
		}

		@Override
		protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
				throws JAXBException, IllegalArgumentException, IllegalAccessException {

			// if (isAssignedTo(serviceId, instanceId)) {

			LifeCycleEvent event = (LifeCycleEvent) msg.getEvent();

			Map<String, String> changeProperties = new HashMap<>();
			changeProperties.put(PROP_ORIGIN, event.getOrigin());
			changeProperties.put(PROP_TARGET, groupId);
			changeProperties.put(PROP_EVENT_NAME, action.toString());

			// log.info(logId() + "onMessage {}", Utils.asJsonString(msg) );

			revisionApi.createOrUpdateRegion(service, instanceId, CHANGE_TYPE_LIFECYCLE, changeProperties);

			// }
		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String epsId, String optionalMessage) {

			CustomEvent eventMsg = (CustomEvent) msg.getEvent();

			Map<String, String> changeProperties = new HashMap<>();
			changeProperties.put(PROP_ORIGIN, eventMsg.getOrigin());
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

	}

	@Override
	protected void clean() {
		if (binding1 != null) {
			admin.removeBinding(binding1);
		}
		if (binding2 != null) {
			admin.removeBinding(binding2);
		}
	}

}
