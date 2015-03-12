package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
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
								managedSet.add(osu.getId());
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

		bindingLifeCycle("*.TRUE.*." + State.STARTING + ".#");

		bindingCustom("*.*." + EpsAction.EPS_ASSIGNED + ".SERVICE");
		bindingCustom("*.*." + EpsAction.EPS_REMOVAL_REQUESTED + ".SERVICE");

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
				String event, String epsId, String optionalMessage) {

			EpsAction action = EpsAction.valueOf(event);
			State serviceState = msg.getTransitions().get(serviceId).getCurrentState();

			if (action == EpsAction.EPS_ASSIGNED) {

				if (!managedSet.contains(epsId) && !serviceState.equals(State.PASSIVE)) {
					createOsu(epsId);
				}

			} else if (action == EpsAction.EPS_REMOVAL_REQUESTED) {

				if (managedSet.contains(epsId)) {
					removeOsu(epsId);
				}
			}
		}

	}

	protected void createOsu(String osuId) {

		managedSet.add(osuId);
		// TODO
	}

	protected void removeOsu(String osuId) {

		managedSet.remove(osuId);
		// TODO
	}

	@Override
	protected void clean() {

	}

}
