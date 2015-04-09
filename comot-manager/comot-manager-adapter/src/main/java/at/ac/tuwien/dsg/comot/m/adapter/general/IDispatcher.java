package at.ac.tuwien.dsg.comot.m.adapter.general;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.AmqpException;

import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;

public interface IDispatcher {

	public void sendLifeCycle(
			Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException;

	public void sendCustom(
			Type targetLevel, CustomEvent event) throws AmqpException, JAXBException;

	public void sendException(
			String serviceId, String instanceId, Exception e) throws AmqpException, JAXBException;
}
