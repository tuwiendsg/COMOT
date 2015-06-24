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

import java.io.IOException;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class SendingChannel extends ARabbitChannel {
	
	public SendingChannel() {
		try {
			this.setUp();
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	public void sendMessage(String type, byte[] bytes) throws IOException {
		this.channel.basicPublish(ARabbitChannel.EXCHANGE_NAME, type, null, bytes);
	}
}
