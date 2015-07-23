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

import at.ac.tuwien.dsg.comot.messaging.rabbitMq.channel.ReceivingChannel;
import at.ac.tuwien.dsg.comot.messaging.api.Consumer;
import at.ac.tuwien.dsg.comot.messaging.api.Message;
import at.ac.tuwien.dsg.comot.messaging.api.MessageReceivedListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMqConsumer implements Consumer, Runnable {	
	private List<MessageReceivedListener> messageListeners;
	private ExecutorService threadPool;
	private ReceivingChannel channel;

	public RabbitMqConsumer(ReceivingChannel channel) {
		this.messageListeners = new ArrayList<>();
		this.threadPool = Executors.newFixedThreadPool(1);
		this.channel = channel;
	}

	@Override
	public RabbitMqConsumer withType(String type) {
		this.channel.bindType(type);
		return this;
	}

	@Override
	public Message getMessage() {
		return this.channel.getDelivery();
	}

	@Override
	public synchronized void addMessageReceivedListener(MessageReceivedListener listener) {
		this.messageListeners.add(listener);

		if (this.messageListeners.size() == 1) {
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

			if (msg != null) {
				this.fireMessageReceived(msg);
			}
		}
	}
}
