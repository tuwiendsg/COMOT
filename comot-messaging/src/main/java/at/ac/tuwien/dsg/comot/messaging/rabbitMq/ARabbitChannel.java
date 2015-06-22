/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.messaging.rabbitMq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;

/**
 *
 * @author vauvenal5
 */
public abstract class ARabbitChannel {
	protected static final String EXCHANGE_NAME = "comot";
	protected ConnectionFactory factory;
	protected Connection connection;
	protected Channel channel;
	private boolean setUp = false;
	
	protected void setUp() throws IOException {
		if(setUp) {
			return;
		}
		
		factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE_NAME, "topic");
		this.setUp = true;
	}
}
