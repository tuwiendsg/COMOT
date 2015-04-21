package at.ac.tuwien.dsg.comot.m.adapter.general;

import java.util.List;

import org.springframework.amqp.core.Binding;

import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;

public interface IProcessor {

	public void setDispatcher(IDispatcher dispatcher);

	public List<Binding> getBindings(
			String queueName, String serviceId);

	public void start(String participantId) throws Exception;

	public void onLifecycleEvent(
			StateMessage msg) throws Exception;

	public void onCustomEvent(
			StateMessage msg) throws Exception;

	public void onExceptionEvent(
			ExceptionMessage msg) throws Exception;

}