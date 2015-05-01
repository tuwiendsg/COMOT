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
package at.ac.tuwien.dsg.comot.m.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;

@Component
@Scope("prototype")
public class UiAdapter extends Processor {

	private static final Logger LOG = LoggerFactory.getLogger(UiAdapter.class);

	@Autowired
	protected InformationClient infoService;

	protected String csInstanceId;
	protected EventOutput eventOutput;

	public static final String MSG_LIFE_CYCLE = "MSG_LIFE_CYCLE";
	public static final String MSG_CUSTOM_EVENT = "MSG_CUSTOM_EVENT";

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName, csInstanceId + ".#"));
		bindings.add(bindingCustom(queueName, csInstanceId + ".#"));
		bindings.add(bindingException(queueName, csInstanceId + ".#"));

		return bindings;
	}

	public void setUiAdapter(String csInstanceId, EventOutput eventOutput) {
		this.csInstanceId = csInstanceId;
		this.eventOutput = eventOutput;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String groupId, Action action,
			String originId, CloudService service, Map<String, Transition> transitions) throws Exception {

		sendToClient(msg);
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		sendToClient(msg);

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String originId)
			throws Exception {
	}

	public void sendToClient(StateMessage msg) {

		try {

			if (eventOutput.isClosed()) {
				LOG.debug("eventOutput.isClosed()");
				clean();
			}

			if (msg.isLifeCycleDefined()) {
				LifeCycleEvent eventLc = (LifeCycleEvent) msg.getEvent();

				Set<OsuInstance> osus = infoService.getService(eventLc.getServiceId()).getSupport();
				msg.getService().setSupport(osus);
			}

			String msgForClient = Utils.asJsonString(msg);

			LOG.trace(logId() + "onMessage {}", msgForClient);

			OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
			eventBuilder.data(String.class, msgForClient);
			eventOutput.write(eventBuilder.build());

		} catch (Exception e) {
			LOG.trace("{}", e);
			LOG.warn("Throwable -> cleanAdapter()");
			clean();
		}
	}

	@Async
	public void checkClient() throws IOException, InterruptedException {

		while (true) {

			try {
				LOG.trace("checking eventOutput");
				OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
				eventBuilder.name("ping");
				eventBuilder.data(String.class, "ping");

				if (eventOutput.isClosed()) {
					clean();
				} else {
					eventOutput.write(eventBuilder.build());
				}
			} catch (Exception e) {
				LOG.trace("{}", e);
				break;
			}
			Thread.sleep(10000);
		}

		LOG.debug("regular check request cleanAdapter()");
		clean();
	}

	protected void clean() {

		manager.clean();

		try {
			if (eventOutput != null) {
				eventOutput.close();
			}
		} catch (IOException e1) {
			LOG.error("{}", e1);
		}
	}

}
