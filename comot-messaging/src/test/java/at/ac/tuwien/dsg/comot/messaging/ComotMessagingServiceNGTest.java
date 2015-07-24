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
import at.ac.tuwien.dsg.comot.messaging.util.Config;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class ComotMessagingServiceNGTest {
	
	ComotMessagingService instance;
	
	@BeforeMethod
	public void beforeTest() {
		Config config = new Config();
		config.setSalsaIp("128.130.172.215")
				.setSalsaPort(8080)
				.setServerCount(1);
		instance = new ComotMessagingService(config);
	}
	
	/**
	 * Test of getServerCount method, of class ComotMessagingService.
	 */
	@Test
	public void testGetServerCount() {
		System.out.println("getServerCount");
		int expResult = 1;
		int result = instance.getServerCount();
		assertEquals(result, expResult);
	}

	/**
	 * Test of setServerCount method, of class ComotMessagingService.
	 */
	@Test
	public void testSetServerCount() {
		System.out.println("setServerCount");
		int count = 0;
		ComotMessagingService instance = null;
		instance.setServerCount(count);
		fail("The test case is a prototype.");
	}

	/**
	 * Test of getRabbitMqMessage method, of class ComotMessagingService.
	 */
	@Test
	public void testGetRabbitMqMessage() {
		System.out.println("getRabbitMqMessage");
		ComotMessagingService instance = null;
		Message expResult = null;
		Message result = instance.getRabbitMqMessage();
		assertEquals(result, expResult);
		fail("The test case is a prototype.");
	}

	/**
	 * Test of getRabbitMqConsumer method, of class ComotMessagingService.
	 */
	@Test
	public void testGetRabbitMqConsumer() {
		System.out.println("getRabbitMqConsumer");
		ComotMessagingService instance = null;
		Consumer expResult = null;
		Consumer result = instance.getRabbitMqConsumer();
		assertEquals(result, expResult);
		fail("The test case is a prototype.");
	}

	/**
	 * Test of getRabbitMqProducer method, of class ComotMessagingService.
	 */
	@Test
	public void testGetRabbitMqProducer() {
		System.out.println("getRabbitMqProducer");
		Config config = null;
		ComotMessagingService instance = null;
		Producer expResult = null;
		Producer result = instance.getRabbitMqProducer();
		assertEquals(result, expResult);
		fail("The test case is a prototype.");
	}	
}
