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

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public abstract class ComotMessagingFactory {
	public static Message getRabbitMqMessage() {
		return new RabbitMqMessage();
	}
	
	public static Consumer getRabbitMqConsumer() {
		return new RabbitMqConsumer();
	}
	
	public static Producer getRabbitMqProducer() {
		return new RabbitMqProducer();
	}
}
