package at.ac.tuwien.dsg.comot.m.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
@Scope("prototype")
public class UiAdapter extends Processor {

	@Autowired
	protected InformationServiceMock infoService;

	protected String csInstanceId;
	protected EventOutput eventOutput;

	public static final String MSG_LIFE_CYCLE = "MSG_LIFE_CYCLE";
	public static final String MSG_CUSTOM_EVENT = "MSG_CUSTOM_EVENT";

	@Override
	public void start() {

	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName, csInstanceId + ".#"));
		bindings.add(bindingCustom(queueName, csInstanceId + ".#"));
		bindings.add(bindingException(queueName, csInstanceId + ".#"));

		return bindings;
	}

	public void setUiAdapter(String csInstanceId, EventOutput eventOutput) {
		this.csInstanceId = csInstanceId;
		this.eventOutput = eventOutput;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId, Action action,
			String originId, CloudService service, Map<String, Transition> transitions) throws Exception {

		sendToClient(msg);
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		sendToClient(msg);

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId, Exception e)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void sendToClient(StateMessage msg) {

		try {

			if (eventOutput.isClosed()) {
				log.debug("eventOutput.isClosed()");
				clean();
			}

			if (msg.isLifeCycleDefined()) {
				LifeCycleEvent eventLc = (LifeCycleEvent) msg.getEvent();

				Set<OfferedServiceUnit> osus = infoService.getSupportingServices(eventLc.getCsInstanceId());
				msg.getService().getInstancesList().get(0).setSupport(osus);
			}

			String msgForClient = Utils.asJsonString(msg);

			log.trace(logId() + "onMessage {}", msgForClient);

			OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
			eventBuilder.data(String.class, msgForClient);
			// eventBuilder.name(MSG_LIFE_CYCLE);
			eventOutput.write(eventBuilder.build());

		} catch (Throwable t) {
			log.warn("Throwable -> cleanAdapter()");
			clean();
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
		clean();
	}

	protected void clean() {

		manager.clean();

		try {
			if (eventOutput != null) {
				eventOutput.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
