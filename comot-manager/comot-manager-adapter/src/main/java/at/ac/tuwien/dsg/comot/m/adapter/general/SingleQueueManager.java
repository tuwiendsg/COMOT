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
package at.ac.tuwien.dsg.comot.m.adapter.general;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SingleQueueManager extends Manager {

	private static final Logger LOG = LoggerFactory.getLogger(SingleQueueManager.class);

	protected SimpleMessageListenerContainer container;

	public void start() {

		admin.declareQueue(new Queue(queueName(), false, false, true));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());

		for (Binding binding : processor.getBindings(queueName(), null)) {
			admin.declareBinding(binding);
		}

		container.setMessageListener(new ProcessorListener(processor));
		container.start();
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
			container.shutdown();
		}
		// TODO clean processor?

		if (admin != null) {
			admin.deleteQueue(queueName());
		}

		LOG.debug("cleaned '{}'", queueName());
	}

	public String queueName() {
		return ADAPTER_QUEUE + participantId;
	}

	@Override
	public void removeInstanceListener(String instanceId) {

	}

}
