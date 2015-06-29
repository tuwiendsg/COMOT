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

import at.ac.tuwien.dsg.comot.messaging.rabbitMq.RabbitMqConsumer;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PropertyBasedDiscovery extends ADiscovery {
	
	private static Logger logger = LoggerFactory.getLogger(PropertyBasedDiscovery.class);
	protected Properties properties;

	@Override
	protected String getHost() {
		try {
			this.properties = new Properties();
			this.properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			return properties.getProperty("rabbitMqServerIp");
		} catch (IOException ex) {
			logger.warn("Property based discovery failed with exception!", ex);
		}
		
		return null;
	}
	
}
