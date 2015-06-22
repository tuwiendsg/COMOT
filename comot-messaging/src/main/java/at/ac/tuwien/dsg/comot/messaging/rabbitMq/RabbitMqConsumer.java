/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author vauvenal5
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
