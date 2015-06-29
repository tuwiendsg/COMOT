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

import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqConsumer;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqMessage;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ReceivingChannel extends ARabbitChannel {
	
	private static Logger logger = LoggerFactory.getLogger(RabbitMqConsumer.class);

	private String queueName;
	QueueingConsumer consumer;
	
	@Override
	public void init() {
		super.init();
		try {
			this.queueName = channel.queueDeclare().getQueue();
			this.consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public void bindType(String type) throws IllegalStateException {
		try {
			channel.queueBind(queueName, ARabbitChannel.EXCHANGE_NAME, type);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public RabbitMqMessage getDelivery() {
		ObjectInputStream in = null;
		try {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			ByteArrayInputStream bais = new ByteArrayInputStream(delivery.getBody());
			in = new ObjectInputStream(bais);
			return (RabbitMqMessage) in.readObject();
		} catch (IOException | 
				ClassNotFoundException | 
				InterruptedException | 
				ShutdownSignalException | 
				ConsumerCancelledException ex) {
			logger.error("Error while receiving message!", ex);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				logger.error("Error while trying to close stream!", ex);
			}
		}

		return null;
	}
}
