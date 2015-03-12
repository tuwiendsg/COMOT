package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class UpdaterAdapter extends Adapter {

	@Autowired
	protected ApplicationContext context;

	@Override
	public void start(String osuInstanceId) {

		container.setMessageListener(new CustomListener(osuInstanceId));

	}

	class CustomListener extends AdapterListener {

		public CustomListener(String adapterId) {
			super(adapterId);
		}

		@Override
		protected void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
				String event, String epsId, String optionalMessage) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	protected void clean() {

	}

}
