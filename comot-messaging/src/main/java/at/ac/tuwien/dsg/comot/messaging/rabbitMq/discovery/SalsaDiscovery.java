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

import at.ac.tuwien.dsg.comot.messaging.rabbitMq.orchestrator.RabbitMQServerCluster;
import at.ac.tuwien.dsg.comot.messaging.util.Config;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import java.util.List;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class SalsaDiscovery extends ADiscovery{
	private RabbitMQServerCluster cluster;
	
	public SalsaDiscovery(RabbitMQServerCluster cluster) {
		this.cluster = cluster;
	}

	@Override
	protected String getHost() {
		List<AssociatedVM> vms = this.cluster.getServerList();
		
		return vms == null ? null : vms.get(0).getIp();
	}
}
