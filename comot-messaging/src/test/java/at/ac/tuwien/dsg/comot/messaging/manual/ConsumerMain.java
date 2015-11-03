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
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.Consumer;
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.Discovery;
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.Message;
import at.ac.tuwien.dsg.cloud.utilities.messaging.api.MessageReceivedListener;
import at.ac.tuwien.dsg.cloud.utilities.messaging.lightweight.ComotMessagingService;
import at.ac.tuwien.dsg.cloud.utilities.messaging.lightweight.util.ServerConfig;
import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMQServerCluster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ConsumerMain {

	/**
	 * @param args the command line arguments
	 *
	 * To execute this class enter on the test VM in the COMOT/comot-messaging
	 * directory the following command mvn exec:java
	 * -Dexec.mainClass="at.ac.tuwien.dsg.comot.messaging.manual.ConsumerMain"
	 * -Dexec.classpathScope="test"
	 */
	public static void main(String[] args) {
		ServerConfig config = new ServerConfig();
		config.setDiscoveryIp("128.130.172.215")
				.setDiscoveryPort(8080)
				.setServerCount(1)
				.setServiceName("ManualTestRabbitService");
		Discovery discovery = new LightweightSalsaDiscovery(config);
		ComotMessagingService instance = new ComotMessagingService(discovery, new RabbitMQServerCluster(config));

		Consumer consumer = instance.getRabbitMqConsumer();
		consumer.addMessageReceivedListener(new MessageReceivedListener() {

			@Override
			public void messageReceived(Message message) {
				System.out.println(String.format("Received message with types %s: %s", message.getTypes()
						.stream()
						.reduce((t1, t2) -> t1 + "," + t2)
						.get(), new String(message.getMessage())));
			}
		});

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		boolean exit = false;

		while (!exit) {

			try {
				System.out.println("Please use one of the following commands:");
				System.out.println("add 'type' - to add a new type to listen to");
				System.out.println("servers # - to change the number of RabbitServers");
				System.out.println("exit - to close the program");
				String input = reader.readLine();

				if (input.equals("exit")) {
					exit = true;
					break;
				}

				String[] splitedInput = input.split(" ");

				if (splitedInput.length == 2) {

					if (splitedInput[0].equals("add")) {
						consumer.withType(splitedInput[1]);
					}

					if (splitedInput[0].equals("servers")) {
						instance.getServerCluster().changeServerCount(Integer.parseInt(splitedInput[1]));
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
