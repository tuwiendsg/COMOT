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

import at.ac.tuwien.dsg.comot.messaging.api.Consumer;
import at.ac.tuwien.dsg.comot.messaging.api.Message;
import at.ac.tuwien.dsg.comot.messaging.api.MessageReceivedListener;
import at.ac.tuwien.dsg.comot.messaging.api.Producer;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMqIT {
	
	private boolean testDone;
	private int hits;
	
	public RabbitMqIT() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
		this.testDone = false;
		this.hits = 0;
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}
	
	/*@Test
	public void simpleProduceConsumeIT() throws Exception {
		byte[] expected = "This is a test message!".getBytes();
		String expectedTypes = "myMsg";
		
		Producer producer = ComotMessagingFactory.getRabbitMqProducer();
		Consumer consumer = ComotMessagingFactory.getRabbitMqConsumer();
		consumer.withType(expectedTypes);
		
		Message msg = ComotMessagingFactory.getRabbitMqMessage();
		msg.setMessage(expected).withType(expectedTypes);
		
		Message msg2 = ComotMessagingFactory.getRabbitMqMessage();
		msg2.setMessage(expected).withType("myTest");
		
		Message[] msgs = new Message[2];
		msgs[0] = msg;
		msgs[1] = msg2;
		
		Message[] actual = new Message[2];
		
		MessageReceivedListener listener = new MessageReceivedListener() {
			@Override
			public void messageRecived(Message message) {
				actual[hits] = message;
				testDone = true;
				hits++;
			}
		};
		
		consumer.addMessageReceivedListener(listener);
		
		producer.sendMessage(msg);
		
		consumer.withType("myTest");
		
		producer.sendMessage(msg2);
		
		while(hits < 2) {
			Thread.sleep(100);
		}
		
		Assert.assertEquals(actual[0], msgs[0]);
		Assert.assertEquals(actual[1], msgs[1]);
		
		
		
		consumer.removeMessageReceivedListener(listener);
	}
	
	@Test
	public void produceToMultipleConsumers() throws Exception {
		byte[] expected = "This is a test message!".getBytes();
		String expectedTypes = "myMsg";
		
		Producer producer = ComotMessagingFactory.getRabbitMqProducer();
		Consumer consumer = ComotMessagingFactory.getRabbitMqConsumer();
		consumer.withType(expectedTypes);
		
		Consumer consumer2 = ComotMessagingFactory.getRabbitMqConsumer();
		consumer2.withType("myTest");
		
		Message msg = ComotMessagingFactory.getRabbitMqMessage();
		msg.setMessage(expected).withType("myMsg").withType("myTest");
		
		producer.sendMessage(msg);
		
		Assert.assertEquals(consumer.getMessage(), msg);
		Assert.assertEquals(consumer2.getMessage(), msg);
	}*/
	
}
