package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.UnsupportedEncodingException;

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

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final String ADAPTER_QUEUE = "ADAPTER_QUEUE_";

	protected String osuInstanceId;

	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;
	protected SimpleMessageListenerContainer container;

	public void startAdapter(String osuInstanceId) {

		this.osuInstanceId = osuInstanceId;

		admin.declareQueue(new Queue(queueName(), false, false, false));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(ADAPTER_QUEUE + osuInstanceId);

		start(osuInstanceId);

		container.start();

		log.info("started adapter '{}'", osuInstanceId);
	}

	public void cleanAdapter() {

	}

	public String queueName() {
		return ADAPTER_QUEUE + osuInstanceId;
	}

	protected abstract void start(String osuInstanceId);

	protected boolean isAssignedTo(String instanceId) {

		for (OfferedServiceUnit osu : infoService.getSupportingServices(instanceId)) {
			if (osu.getId().equals(osuInstanceId)) {
				return true;
			}
		}
		return false;
	}

	protected StateMessage stateMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		StateMessage msg = Utils.asStateMessage(new String(message.getBody(), "UTF-8"));
		return msg;
	}
}
