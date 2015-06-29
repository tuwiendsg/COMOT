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

import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class SendingChannel extends ARabbitChannel {
	
	private static Logger logger = LoggerFactory.getLogger(SendingChannel.class);

	public void sendMessage(String type, RabbitMqMessage msg) {
		ObjectOutputStream out = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(baos);
			out.writeObject(msg);
			this.channel.basicPublish(ARabbitChannel.EXCHANGE_NAME, type, null, baos.toByteArray());
		} catch (IOException ex) {
			logger.error(String.format("Error while sending message with type %s!", type), ex);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				logger.error("Error while trying to close stream!", ex);
			}
		}
	}
}
