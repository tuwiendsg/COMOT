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

import at.ac.tuwien.dsg.comot.messaging.api.Consumer;
import at.ac.tuwien.dsg.comot.messaging.api.Message;
import at.ac.tuwien.dsg.comot.messaging.api.MessageReceivedListener;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMqConsumer implements Consumer, Runnable {

	private List<MessageReceivedListener> messageListeners;
	private ExecutorService threadPool;
	//private TypeHandler typeHandler;

	private ReceivingChannel channel = new ReceivingChannel();

	private static Logger logger = LoggerFactory.getLogger(RabbitMqConsumer.class);

	public RabbitMqConsumer() {
		this.messageListeners = new ArrayList<>();
		this.threadPool = Executors.newFixedThreadPool(1);
		this.channel = new ReceivingChannel();
	}
	
	@Override
	public RabbitMqConsumer withType(String type) {
		this.channel.bindType(type);
		return this;
	}

	@Override
	public Message getMessage() {

		try {
			QueueingConsumer.Delivery delivery = this.channel.getDelivery();
			RabbitMqMessage msg = new RabbitMqMessage();
			msg.setMessage(delivery.getBody());
			
			msg.withType(delivery.getEnvelope().getRoutingKey());

			return msg;
		} catch (InterruptedException ex) {
			logger.error("Exception was catched in RabbitMqConsumer!", ex);
			return null;
		}
	}

	@Override
	public synchronized void addMessageReceivedListener(MessageReceivedListener listener) {
		this.messageListeners.add(listener);
		
		if(this.messageListeners.size() == 1) {
			this.threadPool.execute(this);
		}
	}

	@Override
	public synchronized void removeMessageReceivedListener(MessageReceivedListener listener) {
		this.messageListeners.remove(listener);
	}

	private synchronized void fireMessageReceived(Message msg) {
		this.messageListeners.stream().forEach(listener -> {
			listener.messageRecived(msg);
		});
	}

	@Override
	public void run() {
		while (!this.messageListeners.isEmpty()) {
			Message msg = this.getMessage();
			
			if(msg != null) {
				this.fireMessageReceived(msg);
			}
		}
	}
}
