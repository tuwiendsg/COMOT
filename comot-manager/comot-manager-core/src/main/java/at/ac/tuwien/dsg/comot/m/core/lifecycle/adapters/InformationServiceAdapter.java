package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;

@Component
public class InformationServiceAdapter extends Adapter {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ToscaMapper mapperTosca;

	protected Binding binding1;
	protected Binding binding2;

	@PostConstruct
	public void setUp() {
		startAdapter("INFORMATION_SERVICE");
	}

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

					} else {

					}
				}

			} catch (IOException | JAXBException | IllegalArgumentException e) {
				e.printStackTrace();
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
