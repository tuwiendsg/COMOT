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
package at.ac.tuwien.dsg.comot.messaging.rabbitMq.discovery;

import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PowerDnsDiscoveryNGTest {
	
	public PowerDnsDiscoveryNGTest() {
	}

	/**
	 * Test of getHost method, of class PowerDnsDiscovery.
	 */
	@Test
	public void testGetHost() throws Exception {
		/*System.out.println("getHost");
		PowerDnsDiscovery instance = new PowerDnsDiscovery(InetAddress.getByName("128.130.172.215"), 8080);
		RestTemplate template = Mockito.mock(RestTemplate.class);
		Mockito.when(template.getForObject(Mockito.any(String.class), String.class)).thenReturn("{\"id\":\"novalocal.\",\"url\":\"/servers/localhost/zones/novalocal.\",\"name\":\"novalocal\",\"kind\":\"Native\",\"dnssec\":false,\"account\":\"\",\"masters\":[],\"serial\":2015070901,\"notified_serial\":0,\"last_check\":0,\"soa_edit_api\":\"\",\"soa_edit\":\"\",\"records\":[{\"name\":\"novalocal\",\"type\":\"SOA\",\"ttl\":3600,\"disabled\":false,\"content\":\"a.misconfigured.powerdns.server hostmaster.novalocal 2015070901 10800 3600 604800 3600\"},{\"name\":\"rabbitmqservervm-0.novalocal\",\"type\":\"A\",\"ttl\":86400,\"disabled\":false,\"content\":\"10.99.0.73\"},{\"name\":\"rabbitmqservervm-1.novalocal\",\"type\":\"A\",\"ttl\":86400,\"disabled\":false,\"content\":\"10.99.0.74\"},{\"name\":\"rabbitmqservervm-2.novalocal\",\"type\":\"A\",\"ttl\":86400,\"disabled\":false,\"content\":\"10.99.0.77\"}],\"comments\":[]}");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(template);
		String expResult = "10.99.0.73";
		String result = instance.getHost();
		assertEquals(result, expResult);*/
	}
	
}
