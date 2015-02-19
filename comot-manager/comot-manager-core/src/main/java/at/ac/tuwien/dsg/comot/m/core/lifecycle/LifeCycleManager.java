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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Map<String, ManagerOfServiceInstance> managers = new HashMap<>();
	
	@Autowired
	protected ApplicationContext context;
	
	public static final String EXCHANGE_NAME = "SERVICE_LIFECYCLE_INFORMATION";
	public static final String SERVER = "localhost";

	protected Connection connection;
	protected Channel channel;

	public void setUp() throws IOException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(SERVER);
		connection = factory.newConnection();

		channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME, "topic");
	}

	public void executeAction(String serviceId, String csInstanceId, String groupId, Action action) {

	}

	public void executeAction(String serviceId, String groupId, Action action) {

	}

	
	public void createNewInstance(String serviceId, String csInstanceId) throws JAXBException, IOException{
		
		if (managers.containsKey(serviceId)) {
			managers.get(serviceId).createNewInstance(serviceId, csInstanceId);
		} else {
			ManagerOfServiceInstance manager = context.getBean(ManagerOfServiceInstance.class);
			manager.createNewInstance(csInstanceId, csInstanceId);
			managers.put(csInstanceId, manager);
		}
		
		// get info from infoService
		// create new lifecycle manager
		// inform about the change
		
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

}
