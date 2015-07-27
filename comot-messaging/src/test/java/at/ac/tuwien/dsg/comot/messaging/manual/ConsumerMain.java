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
package at.ac.tuwien.dsg.comot.messaging.manual;

import at.ac.tuwien.dsg.comot.messaging.ComotMessagingService;
import at.ac.tuwien.dsg.comot.messaging.api.Consumer;
import at.ac.tuwien.dsg.comot.messaging.api.Message;
import at.ac.tuwien.dsg.comot.messaging.util.Config;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ConsumerMain {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Config config = new Config();
		config.setSalsaIp("128.130.172.215")
				.setSalsaPort(8080)
				.setServerCount(1);
		ComotMessagingService instance = new ComotMessagingService(config);
		
		Consumer consumer = instance.getRabbitMqConsumer().withType(args[0]);
		
		while(true) {
			Message msg = consumer.getMessage();
			System.out.println(new String(msg.getMessage()));
		}
	}
	
	
	
}
