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
package at.ac.tuwien.dsg.comot.messaging.util;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class Config {	
	private String salsaIp;
	private int salsaPort;
	private int serverCount;
	private String serviceName;

	public String getSalsaIp() {
		return salsaIp;
	}

	public Config setSalsaIp(String salsaIp) {
		this.salsaIp = salsaIp;
		return this;
	}

	public int getSalsaPort() {
		return salsaPort;
	}

	public Config setSalsaPort(int salsaPort) {
		this.salsaPort = salsaPort;
		return this;
	}

	public int getServerCount() {
		return serverCount;
	}

	public Config setServerCount(int rabbitServerCount) {
		this.serverCount = rabbitServerCount;
		return this;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Config setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}
}
