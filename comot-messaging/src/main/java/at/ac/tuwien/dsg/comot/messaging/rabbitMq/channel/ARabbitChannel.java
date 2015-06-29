/* 
 * Copyright 2015 Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.ac.tuwien.dsg.comot.messaging.rabbitMq.channel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public abstract class ARabbitChannel {

	protected static final String EXCHANGE_NAME = "comot";
	protected ConnectionFactory factory;
	protected Connection connection;
	protected Channel channel;

	protected Properties properties;

	public ARabbitChannel() {
		try {
			this.properties = new Properties();
			this.properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			String host = properties.getProperty("rabbitMqServerIp");
			
			factory = new ConnectionFactory();
			factory.setHost(host);
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "topic");
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}	
}
