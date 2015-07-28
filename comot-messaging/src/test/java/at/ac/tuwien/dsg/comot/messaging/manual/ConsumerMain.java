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
import at.ac.tuwien.dsg.comot.messaging.api.MessageReceivedListener;
import at.ac.tuwien.dsg.comot.messaging.util.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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

		Consumer consumer = instance.getRabbitMqConsumer();
		consumer.addMessageReceivedListener(new MessageReceivedListener() {

			@Override
			public void messageRecived(Message message) {
				System.out.println(String.format("Recived message with types %s: %s", message.getTypes()
						.stream()
						.reduce((t1, t2) -> t1 + "," + t2)
						.get(), new String(message.getMessage())));
			}
		});

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		boolean exit = false;

		while (!exit) {

			try {
				System.out.println("Please add the channel type you want to listen to with 'add type' or exit");
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
					
					if(splitedInput[0].equals("servers")) {
						instance.setServerCount(Integer.getInteger(splitedInput[1]));
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
