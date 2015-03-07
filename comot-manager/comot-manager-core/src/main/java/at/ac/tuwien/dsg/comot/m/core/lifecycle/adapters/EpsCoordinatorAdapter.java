package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Quality;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class EpsCoordinatorAdapter extends Adapter {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ToscaMapper mapperTosca;

	protected Binding binding1;
	protected Binding binding3;
	protected Binding binding4;

	protected Set<String> createdOsus = new HashSet<String>();

	@PostConstruct
	public void setUp() {
		startAdapter("EPS_COORDINATOR");

		for (OfferedServiceUnit osu : infoService.getOsus().values()) {
			try {
				for (Resource res : osu.getResources()) {
					if (res.getType().getName().equals(InformationServiceMock.TYPE_STATIC_SERVICE)) {
						for (Resource res2 : res.getContainsResources()) {
							if (res2.getType().getName().equals(InformationServiceMock.ADAPTER_CLASS)) {
								Adapter adapter = (Adapter) context.getBean(Class.forName(res2.getName()));
								adapter.startAdapter(osu.getId());
								createdOsus.add(osu.getId());
							}
						}
					}
				}
			} catch (BeansException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start(String osuInstanceId) {

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				"*." + EpsAction.EPS_ASSIGNED + ".SERVICE", null);
		binding3 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				"*." + EpsAction.EPS_REMOVAL_REQUESTED + ".SERVICE", null);
		binding4 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.STARTING + ".#", null);

		admin.declareBinding(binding1);
		container.setMessageListener(new CustomListener(osuInstanceId));

	}

	class CustomListener extends AdapterListener {

		public CustomListener(String adapterId) {
			super(adapterId);
		}

		@Override
		protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions) {

			if (action == Action.STARTED) {

				for (OfferedServiceUnit osu : infoService.getSupportingServices(serviceId, instanceId)) {
					for (Quality quality : osu.getQualities()) {
						if (quality.getType().getName().equals(InformationServiceMock.TYPE_ACTION)
								&& quality.getName().equals(Action.STARTED.toString())) {

							createOsu(osu.getId());
						}
					}
				}
			}
		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String optionalMessage) {

			EpsAction action = EpsAction.valueOf(event);
			String osuId = optionalMessage;
			State serviceState = lcManager.getCurrentState(instanceId, groupId);

			if (action == EpsAction.EPS_ASSIGNED) {

				if (!createdOsus.contains(osuId) && !serviceState.equals(State.PASSIVE)) {
					createOsu(osuId);
				}

			} else if (action == EpsAction.EPS_REMOVAL_REQUESTED) {

				if (!createdOsus.contains(osuId)) {
					removeOsu(osuId);
				}

			}
		}

	}

	protected void createOsu(String osuId) {

		createdOsus.add(osuId);
		// TODO
	}

	protected void removeOsu(String osuId) {

		createdOsus.remove(osuId);
		// TODO
	}

	@Override
	protected void clean() {
		if (binding1 != null) {
			admin.removeBinding(binding1);
		}
		if (binding3 != null) {
			admin.removeBinding(binding3);
		}
		if (binding4 != null) {
			admin.removeBinding(binding4);
		}
	}

}
