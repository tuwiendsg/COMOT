/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;

public class UtilsTest {

	private final static Logger log = LoggerFactory.getLogger(UtilsTest.class);

	public static final String TEST_FILE_BASE = "./../resources/test/";

	public static void sleepInfinit() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("{}", e);
			}
		}
	}

	public static void sleepSeconds(int seconds) {
		try {
			log.debug("Waiting {} seconds", seconds);
			Thread.sleep(seconds * 1000);

		} catch (InterruptedException e) {
			log.error("{}", e);
		}
	}

	public static ServiceUnit getServiceUnit(CloudService service, String id) {

		for (ServiceTopology topo : Navigator.getAllTopologies(service)) {
			for (ServiceUnit unit : topo.getServiceUnits()) {
				if (unit.getId().equals(id)) {
					return unit;
				}
			}
		}

		return null;

	}

}
