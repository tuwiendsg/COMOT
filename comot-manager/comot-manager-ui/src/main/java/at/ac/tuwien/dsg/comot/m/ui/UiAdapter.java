package at.ac.tuwien.dsg.comot.m.ui;

import java.io.IOException;
import java.util.Set;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.UtilsLc;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.Adapter;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

@Component
@Scope("prototype")
public class UiAdapter extends Adapter {

	@Autowired
	protected MonitoringClient monitoring;

	protected Binding binding1;
	protected Binding binding2;

	protected String csInstanceId;
	protected EventOutput eventOutput;

	public static final String MSG_LIFE_CYCLE = "MSG_LIFE_CYCLE";
	public static final String MSG_CUSTOM_EVENT = "MSG_CUSTOM_EVENT";

	@Override
	public void start(String id) {

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				csInstanceId + ".#", null);
		binding2 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				csInstanceId + ".#", null);

		admin.declareBinding(binding1);
		admin.declareBinding(binding2);

		container.setMessageListener(new CustomListener());

	}

	public void setUiAdapter(String csInstanceId, EventOutput eventOutput) {
		this.csInstanceId = csInstanceId;
		this.eventOutput = eventOutput;
	}

	class CustomListener implements MessageListener {

		@Override
		public void onMessage(Message message) {
			try {

				if (eventOutput.isClosed()) {
					log.debug("eventOutput.isClosed()");
					cleanAdapter();
				}
				StateMessage msg = UtilsLc.stateMessage(message);

				if (msg.isLifeCycleDefined()) {
					LifeCycleEvent eventLc = (LifeCycleEvent) msg.getEvent();

					Set<OfferedServiceUnit> osus = infoService.getSupportingServices(eventLc.getServiceId(),
							eventLc.getCsInstanceId());
					eventLc.getService().getInstancesList().get(0).setSupport(osus);
				}

				String msgForClient = Utils.asJsonString(msg);

				log.trace(logId() + "onMessage {}", msgForClient);

				OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
				eventBuilder.data(String.class, msgForClient);
				// eventBuilder.name(MSG_LIFE_CYCLE);
				eventOutput.write(eventBuilder.build());

			} catch (Throwable t) {
				log.warn("Throwable -> cleanAdapter()");
				cleanAdapter();
			}

		}
	}

	@Async
	public void checkClient() throws IOException, InterruptedException {

		while (true) {

			try {
				log.trace("checking eventOutput");
				OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
				eventBuilder.name("ping");
				eventBuilder.data(String.class, "ping");
				eventOutput.write(eventBuilder.build());

			} catch (Throwable e) {
				break;
			}
			Thread.sleep(10000);
		}

		log.debug("regular check request cleanAdapter()");
		cleanAdapter();
	}

	@Override
	protected void clean() {
		if (binding1 != null) {
			admin.removeBinding(binding1);
		}
		if (binding2 != null) {
			admin.removeBinding(binding2);
		}

		try {
			if (eventOutput != null) {
				eventOutput.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// class Client {
	//
	// String csInstanceId;
	// EventOutput eventOutput;
	//
	// public Client(String csInstanceId, EventOutput eventOutput) {
	// super();
	// this.csInstanceId = csInstanceId;
	// this.eventOutput = eventOutput;
	// }
	//
	// }

}
