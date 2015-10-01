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

import at.ac.tuwien.dsg.cloud.salsa.messaging.DSGQueueAdaptorLightweight.discovery.LightweightSalsaDiscovery;
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.Discovery;
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.Message;
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.Producer;
import at.ac.tuwien.dsg.cloud.utilities.messaging.lightweight.ComotMessagingService;
import at.ac.tuwien.dsg.cloud.utilities.messaging.lightweight.util.ServerConfig;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMQServerCluster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 *
 * To execute this class enter on the test VM in the COMOT/comot-messaging
 * directory the following command mvn exec:java
 * -Dexec.mainClass="at.ac.tuwien.dsg.comot.messaging.manual.ProducerMain"
 * -Dexec.classpathScope="test"
 */
public class ProducerMain {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		ServerConfig config = new ServerConfig();
		config.setSalsaIp("128.130.172.215")
				.setSalsaPort(8080)
				.setServerCount(1)
				.setServiceName("ManualTestRabbitService");
		Discovery discovery = new LightweightSalsaDiscovery(config);
		ComotMessagingService instance = new ComotMessagingService(discovery, new RabbitMQServerCluster(config));

		Producer producer = instance.getRabbitMqProducer();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean exit = false;

		while (!exit) {
			try {
				System.out.println("Enter message in the format type;message or exit");
				String input = reader.readLine();

				if (input.equals("exit")) {
					exit = true;
					break;
				}

				String[] splitedInput = input.split(";");

				if (splitedInput.length == 2) {
					String type = splitedInput[0];

					Message msg = instance.getRabbitMqMessage();
					msg.setMessage(splitedInput[1].getBytes());
					msg.withType(type);

					producer.sendMessage(msg);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

}
