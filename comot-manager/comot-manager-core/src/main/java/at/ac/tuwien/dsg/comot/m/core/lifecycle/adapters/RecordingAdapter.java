package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
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

		container.setMessageListener(new CustomListener());

	}

	class CustomListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			try {

				StateMessage msg = stateMessage(message);
				String instanceId = msg.getCsInstanceId();

				if (isAssignedTo(instanceId)) {

					if (msg.getEvent().isLifeCycleDefined()) {
						insertNewVersion(msg.getEvent().getService(), instanceId, msg.getAction());

					} else {
						// TODO store custom events
					}
				}

			} catch (IOException | JAXBException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}

	public void insertNewVersion(CloudService service, String csInstanceId, Action action)
			throws IllegalArgumentException, IllegalAccessException {

		revisionApi.createOrUpdateRegion(service, csInstanceId, action.toString());

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
