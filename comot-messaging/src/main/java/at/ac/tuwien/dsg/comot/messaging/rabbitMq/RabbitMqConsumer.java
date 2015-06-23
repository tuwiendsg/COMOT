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
import java.io.IOException;
import java.util.List;
import javax.swing.event.EventListenerList;

/**
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMqConsumer extends ARabbitChannel implements Consumer {
	
	private List<MessageReceivedListener> messageListeners;
	
	public RabbitMqConsumer() {
		
	}

	@Override
	public Message getMessage() throws IOException {
		this.setUp();
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, ARabbitChannel.EXCHANGE_NAME, );
	}

	@Override
	public void addMessageReceivedListener(MessageReceivedListener listener) {
		this.messageListeners.add(listener);
	}

	@Override
	public void removeMessageReceivedListener(MessageReceivedListener listener) {
		this.messageListeners.remove(listener);
	}
	
	private void fireMessageReceived(Message msg) {
		this.messageListeners.stream().forEach(listener -> {listener.messageRecived(msg);});
	}

	@Override
	public Consumer withType(String type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
