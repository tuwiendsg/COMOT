package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Action;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;

	public static final String EXCHANGE_NAME = "SERVICE_LIFECYCLE_INFORMATION";
	public static final String SERVER = "localhost";

	protected Connection connection;
	protected Channel channel;

	protected Map<String, ManagerOfServiceInstance> managers = new HashMap<>();

	public void setUp() throws IOException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(SERVER);
		connection = factory.newConnection();

		channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, "topic");
	}

	public void send(String routingKey, String message) throws IOException {

		channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());

		System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

	}

	public void cleanUp() throws IOException {
		if (channel != null) {
			channel.close();
		}
		if (connection != null) {
			connection.close();
		}
	}

	public void executeAction(String serviceId, String csInstanceId, String groupId, Action action) throws IOException,
			JAXBException {

		ManagerOfServiceInstance manager;

		if (action.equals(Action.CREATE_NEW_INSTANCE)) {

			if (managers.containsKey(csInstanceId)) {
				return;
			}

			manager = context.getBean(ManagerOfServiceInstance.class);
			StateMessage message = manager.createNewInstance(csInstanceId, csInstanceId);
			managers.put(csInstanceId, manager);

			String msg = Utils.asJsonString(message);

			log.info(msg);

			send(serviceId + "." + csInstanceId + ".#", msg);

		}

		if (!managers.containsKey(csInstanceId)) {
			throw new ComotIllegalArgumentException("Instance '" + csInstanceId + "' has no managed life-cycle");
		}

		manager = managers.get(csInstanceId);
		manager.executeAction(groupId, action);

	}

	public void executeAction(String serviceId, String groupId, Action action) {

		for (ManagerOfServiceInstance manager : managers.values()) {
			if (manager.getServiceGroup().getId().equals(serviceId)) {
				manager.executeAction(groupId, action);
			}
		}

	}

}
