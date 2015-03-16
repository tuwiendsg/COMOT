package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general.AdapterCore;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general.AdapterListener;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general.AdapterManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general.SingleQueueAdapter;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class EpsCoordinatorAdapter extends SingleQueueAdapter {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ToscaMapper mapperTosca;

	@PostConstruct
	public void setUp() {
		startAdapter("EPS_COORDINATOR");

		for (OfferedServiceUnit osu : infoService.getOsus().values()) {
			try {
				createStaticEps(osu);
			} catch (BeansException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start(String osuInstanceId) {

		bindingCustom("*.*." + EpsAction.EPS_DYNAMIC_REQUESTED + ".SERVICE");
		bindingCustom("*.*." + EpsAction.EPS_DYNAMIC_REMOVED + ".SERVICE");

		// TODO bindings for dynamic eps created

		container.setMessageListener(new CustomListener(osuInstanceId));

	}

	class CustomListener extends AdapterListener {

		public CustomListener(String adapterId) {
			super(adapterId);
		}

		@Override
		protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
				throws ClassNotFoundException, IOException {

		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String epsId, String optionalMessage) throws ClassNotFoundException {

			EpsAction action = EpsAction.valueOf(event);
			State serviceState = msg.getTransitions().get(serviceId).getCurrentState();

			if (action == EpsAction.EPS_DYNAMIC_REQUESTED) {

				createDynamicEps(epsId);

			} else if (action == EpsAction.EPS_DYNAMIC_CREATED) {
				// TODO this replace by observation of lifecycle of the dynamic eps

				managedSet.add(epsId);

			} else if (action == EpsAction.EPS_DYNAMIC_REMOVED) {

				removeDynamicEps(epsId);

			}
		}

	}

	protected void createStaticEps(OfferedServiceUnit osu) throws ClassNotFoundException {

		for (Resource res : osu.getResources()) {
			if (res.getType().getName().equals(InformationServiceMock.ADAPTER_CLASS)) {

				String epsId = osu.getId();
				Class<?> clazz = Class.forName(res.getName());

				AdapterCore adapter = (AdapterCore) context.getBean(clazz);

				admin.declareQueue(new Queue(AdapterManager.queueNameAssignment(epsId), false, false,
						false));

				adapter.startAdapter(epsId);
				managedSet.add(epsId);

				break;
			}
		}

	}

	protected void createDynamicEps(String epsId) {

		// on EPS_INSTANTIATE_REQUEST

		// create the service

		// EPS creates its assignment queue on its own

		// EPS writes itself to the DB

		// coordinator listens on the lifecycle and notices when the eps is created

		throw new UnsupportedOperationException("createDynamicEps");
	}

	protected void removeDynamicEps(String epsId) {

		if (managedSet.contains(epsId)) {
			// TODO

			managedSet.remove(epsId);
		}
	}

	@Override
	protected void clean() {

	}

}
