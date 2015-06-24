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
package at.ac.tuwien.dsg.comot.messaging.rabbitMq;

import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ReceivingChannel extends ARabbitChannel {
	
	private String queueName;
	QueueingConsumer consumer;
	
	public ReceivingChannel() {
		try {
			this.setUp();
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	@Override
	protected void setUp() throws IOException {
		if (this.setUp) {
			return;
		}

		super.setUp();

		this.queueName = channel.queueDeclare().getQueue();
		this.consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);
	}
	
	public void bindType(String type) throws IllegalStateException {
		try {
			channel.queueBind(queueName, ARabbitChannel.EXCHANGE_NAME, type);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	public QueueingConsumer.Delivery getDelivery() throws InterruptedException {
		return consumer.nextDelivery();
	}
}
