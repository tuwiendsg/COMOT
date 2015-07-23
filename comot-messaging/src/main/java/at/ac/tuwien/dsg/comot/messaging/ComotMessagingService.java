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
package at.ac.tuwien.dsg.comot.messaging;

import at.ac.tuwien.dsg.comot.messaging.api.Consumer;
import at.ac.tuwien.dsg.comot.messaging.api.Message;
import at.ac.tuwien.dsg.comot.messaging.api.Producer;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqConsumer;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqMessage;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqProducer;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.channel.ReceivingChannel;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.channel.SendingChannel;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.discovery.SalsaDiscovery;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.orchestrator.RabbitMQServerCluster;
import at.ac.tuwien.dsg.comot.messaging.util.Config;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ComotMessagingService {
	
	private RabbitMQServerCluster rabbitCluster;
	
	public ComotMessagingService(Config config) {
		rabbitCluster = new RabbitMQServerCluster(config);
		rabbitCluster.deploy();
	}
	
	public void setServerCount(int count) {
		rabbitCluster.changeServerCount(count);
	}
	
	public int getServerCount() {
		return rabbitCluster.getServerList().size();
	}
	
	public Message getRabbitMqMessage() {
		return new RabbitMqMessage();
	}
	
	public Consumer getRabbitMqConsumer() {
		return new RabbitMqConsumer(new ReceivingChannel(new SalsaDiscovery(rabbitCluster)));
	}
	
	public Producer getRabbitMqProducer(Config config) {
		return new RabbitMqProducer(new SendingChannel(new SalsaDiscovery(rabbitCluster)));
	}

	/**
	 * @param args the command line arguments
	 */
	/*public static void main(String[] args) {
		// TODO code application logic here
	}*/
	
}
