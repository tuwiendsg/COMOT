package at.ac.tuwien.dsg.comot.m.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessageLifeCycle;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class CoordinatorAdapter extends Processor {

	protected String serviceId;
	protected Coordinator coordinator;
	protected Signal signal;
	protected ComotMessage response;

	@Override
	public List<Binding> getBindings(String queueName, String notUsed) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName, serviceId + ".#"));
		bindings.add(bindingCustom(queueName, serviceId + ".#"));
		bindings.add(bindingException(queueName, serviceId + ".#"));

		return bindings;
	}

	public CoordinatorAdapter(String serviceId, Coordinator coordinator) {
		super();
		this.serviceId = serviceId;
		this.signal = new Signal();
		this.coordinator = coordinator;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String groupId, Action action,
			String originId, CloudService service, Map<String, Transition> transitions) throws Exception {
		process(msg.getEvent(), false, msg);
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {
		process(msg.getEvent(), false, msg);
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String originId)
			throws Exception {

		if (msg instanceof ExceptionMessageLifeCycle) {
			process(((ExceptionMessageLifeCycle) msg).getEvent(), true, msg);
		}

	}

	public void process(AbstractEvent event, boolean exception, ComotMessage msg) {

	}

	public void send() throws ComotException, InterruptedException, AmqpException, JAXBException,
			ComotLifecycleException {

		sendInternal();

		long count = 0;

		while (signal.result == null && count < Coordinator.TIMEOUT) {
			Thread.sleep(100);
			count = count + 100;
		}

		manager.clean();

		if (signal.result == null) {
			throw new ComotException("Timeout waiting for event");
		} else if (signal.result) {
			return;
		} else {
			throw new ComotLifecycleException(((ExceptionMessageLifeCycle) response).getMessage());
		}

	}

	public void sendInternal() throws AmqpException, JAXBException {

	}

	protected void clean() {
		manager.clean();
	}

	public class Signal {

		public Boolean result = null;

	}
}
