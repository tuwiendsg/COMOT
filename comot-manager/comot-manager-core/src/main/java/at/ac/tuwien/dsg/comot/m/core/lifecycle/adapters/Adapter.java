package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.UnsupportedEncodingException;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

public abstract class Adapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String ADAPTER_QUEUE = "ADAPTER_QUEUE_";

	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

	private String adapterId;
	protected SimpleMessageListenerContainer container;

	public void startAdapter(String adapterId) {

		this.adapterId = adapterId;

		admin.declareQueue(new Queue(queueName(), false, false, false));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());

		start(adapterId);

		container.start();

		log.info("started adapter '{}'", adapterId);
	}

	@PreDestroy
	public void cleanAdapter() {

		log.info("cleaning requested '{}'", queueName());

		if (container != null) {
			container.stop();
		}
		clean();
		if (admin != null) {
			admin.deleteQueue(queueName());
		}

		log.info("cleaned '{}'", queueName());
	}

	public String queueName() {
		return ADAPTER_QUEUE + adapterId;
	}

	protected abstract void start(String osuInstanceId);

	protected abstract void clean();

	protected boolean isAssignedTo(String instanceId) {

		for (OfferedServiceUnit osu : infoService.getSupportingServices(instanceId)) {
			if (osu.getId().equals(adapterId)) {
				return true;
			}
		}
		return false;
	}

	public static StateMessage stateMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		StateMessage msg = Utils.asStateMessage(new String(message.getBody(), "UTF-8"));
		return msg;
	}

	protected String logId() {
		return "[" + adapterId + "] ";
	}
}
