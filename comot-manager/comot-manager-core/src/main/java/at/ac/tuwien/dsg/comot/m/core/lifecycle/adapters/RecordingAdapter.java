package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class RecordingAdapter extends Adapter {

	public static final String CHANGE_TYPE_LIFECYCLE = "LifeCycleEvent";
	public static final String CHANGE_TYPE_CUSTOM = "CustomEvent";

	public static final String PROP_ORIGIN = "origin";
	public static final String PROP_MSG = "msg";
	public static final String PROP_TARGET = "target";
	public static final String PROP_EVENT_NAME = "eventName";

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ToscaMapper mapperTosca;

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
				throws JAXBException {

			// if (isAssignedTo(serviceId, instanceId)) {

			try {

				EventMessage event = msg.getEvent();

				Map<String, String> changeProperties = new HashMap<>();
				changeProperties.put(PROP_ORIGIN, event.getOrigin());
				changeProperties.put(PROP_TARGET, event.getGroupId());
				changeProperties.put(PROP_EVENT_NAME, event.getAction().toString());
				if (event.getMessage() != null) {
					changeProperties.put(PROP_MSG, event.getMessage());
				}

				// log.info(logId() + "onMessage {}", Utils.asJsonString(msg) );

				revisionApi.createOrUpdateRegion(service, instanceId, CHANGE_TYPE_LIFECYCLE, changeProperties);

			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			// }
		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String optionalMessage) {
			// TODO Auto-generated method stub

		}

	}

	public Object getRevision(String serviceId, String objectId, Long timestamp) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException, ComotException {

		if (!revisionApi.verifyObject(serviceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + serviceId + " there is no managed object "
					+ objectId);
		}

		Object obj = revisionApi.getRevision(serviceId, objectId, timestamp);

		if (obj == null) {
			throw new ComotIllegalArgumentException("There is no revision of service" + serviceId + ", object="
					+ objectId + " at time=" + timestamp + " ");

		}

		return obj;
	}

	public List<Change> getAllChanges(String serviceId, String objectId, Long from, Long to)
			throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException, ComotException {

		if (!revisionApi.verifyObject(serviceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + serviceId + " there is no managed object "
					+ objectId);
		}

		Change change = revisionApi.getAllChanges(serviceId, objectId, from, to);

		List<Change> list = new ArrayList<>();

		while (change != null) {
			list.add(change);
			change = change.getTo().getEnd();
		}

		return list;
	}

	public List<ManagedObject> getManagedObjects(String serviceId) {

		List<ManagedObject> list = revisionApi.getManagedObjects(serviceId);
		return list;

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
