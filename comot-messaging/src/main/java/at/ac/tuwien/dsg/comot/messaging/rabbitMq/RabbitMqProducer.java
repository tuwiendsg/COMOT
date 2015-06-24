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

import at.ac.tuwien.dsg.comot.messaging.api.Message;
import at.ac.tuwien.dsg.comot.messaging.api.Producer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMqProducer implements Producer {

	private static Logger logger = LoggerFactory.getLogger(RabbitMqProducer.class);

	private SendingChannel channel = new SendingChannel();

	@Override
	public void sendMessage(Message message) {
		if (!(message instanceof RabbitMqMessage)) {
			throw new IllegalArgumentException("Message is not a RabbitMqMessage!");
		}

		RabbitMqMessage msg = (RabbitMqMessage) message;

		msg.getTypes().stream().forEach(type -> {
			try {
					//todo: maybe make message serializable and send complete message instead of only body
				//this would allow us to have all types on the receiving side!!
				//use jackson?
				this.channel.sendMessage(type, msg.getMessage());
			} catch (IOException ex) {
				logger.error(String.format("Error while sending message to %s queue!", type), ex);
			}
		});

	}

}
