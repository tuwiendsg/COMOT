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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class ProcessorListener implements MessageListener {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessorListener.class);

	protected Processor processor;

	public ProcessorListener(Processor processor) {
		this.processor = processor;
	}

	@Override
	public void onMessage(Message message) {

		String serviceId = null;
		ComotMessage comotMsg;
		try {

			try {
				comotMsg = UtilsLc.comotMessage(message);
			} catch (Exception e) {

				String body = new String(message.getBody(), "UTF-8");
				LOG.error("Failed to unmarshall message: {}", body);
				LOG.error("{}", e);
				throw e;
			}

			if (comotMsg instanceof StateMessage) {

				StateMessage msg = (StateMessage) comotMsg;
				serviceId = msg.getEvent().getServiceId();
				String groupId = msg.getEvent().getGroupId();
				String origin = msg.getEvent().getOrigin();

				if (msg.getEvent() instanceof LifeCycleEvent) {
					LifeCycleEvent event = (LifeCycleEvent) msg.getEvent();

					Action action = event.getAction();
					CloudService service = msg.getService();
					Map<String, Transition> transitions = msg.getTransitions();

					LOG.info(processor.logId()
							+ "onLifecycleEvent: service={}, group={}, action={}, origin={}",
							serviceId, groupId, action, origin);

					processor.onLifecycleEvent(msg, serviceId, groupId, action, origin, service,
							transitions);

				} else {

					CustomEvent event = (CustomEvent) msg.getEvent();

					String optionalMessage = event.getMessage();
					String eventName = event.getCustomEvent();
					String epsId = event.getEpsId();

					LOG.info(processor.logId()
							+ "onCustomEvent: service={}, group={}, epsId={}, event={}, origin={}",
							serviceId, groupId, epsId, eventName, origin);

					processor.onCustomEvent(msg, serviceId, groupId, eventName, epsId, origin,
							optionalMessage);
				}

			} else if (comotMsg instanceof ExceptionMessage) {
				ExceptionMessage msg = (ExceptionMessage) comotMsg;

				serviceId = msg.getServiceId();
				String originId = msg.getOrigin();

				LOG.info(processor.logId() + "onExceptionEvent: {}", msg);

				processor.onExceptionEvent(msg, serviceId, originId);
			}

		} catch (Exception e) {
			try {
				processor.getManager().sendException(serviceId, e);
			} catch (Exception e1) {
				LOG.error("{}", e1);
			}
			LOG.error("{}", e);
		}

	}
}
