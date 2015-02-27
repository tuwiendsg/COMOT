package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
// @Scope("prototype") //for some reason this creates multip0le instances
public class MonitoringAdapter extends Adapter {

	@Autowired
	protected MonitoringClient monitoring;

	protected Binding binding1;
	protected Binding binding2;

	@Override
	public void start(String osuInstanceId) {

		OfferedServiceUnit osu = infoService.getOsus().get(osuInstanceId);
		String ip = null;
		String port = null;

		for (Resource resource : osu.getResources()) {
			if (resource.getName().equals(InformationServiceMock.PUBLIC_INSTANCE)) {
				for (Resource res : resource.getContainsResources()) {
					if (res.getType().getName().equals(InformationServiceMock.IP)) {
						ip = res.getName();
					} else if (res.getType().getName().equals(InformationServiceMock.PORT)) {
						port = res.getName();
					}
				}
			}
		}

		monitoring.setHost(ip);
		monitoring.setPort(new Integer(port));

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.IDLE + ".#", null);
		binding2 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.UNDEPLOYMENT + ".#", null);

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
				String serviceId = msg.getServiceId();
				Action action = msg.getAction();

				log.info("onMessage {}", Utils.asJsonString(msg));

				if (isAssignedTo(instanceId)) {
					if (action.equals(Action.PREPARED)) {

					}
				}

			} catch (JAXBException | IOException e) {
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
