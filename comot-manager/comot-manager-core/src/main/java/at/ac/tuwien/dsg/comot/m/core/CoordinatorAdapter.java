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
package at.ac.tuwien.dsg.comot.m.core;

import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationContext;

import at.ac.tuwien.dsg.comot.m.adapter.general.Bindings;
import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.adapter.general.SingleQueueManager;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessageLifeCycle;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public abstract class CoordinatorAdapter extends Processor {

	protected String serviceId;
	protected Coordinator coordinator;
	protected Signal signal;
	protected Exception exception;
	protected AbstractEvent event;

	@Override
	public Bindings getBindings(String notUsed) {

		return new Bindings()
				.addLifecycle(serviceId + ".#")
				.addCustom(serviceId + ".#")
				.addException(serviceId + ".#");
	}

	public CoordinatorAdapter(AbstractEvent event, Coordinator coordinator, ApplicationContext context)
			throws Exception {
		super();
		this.serviceId = event.getServiceId();
		this.signal = new Signal();
		this.coordinator = coordinator;
		this.event = event;

		Manager manager = context.getBean(SingleQueueManager.class);
		manager.start("C_" + UUID.randomUUID().toString(), this);

	}

	@Override
	public void onLifecycleEvent(String serviceId, String groupId, Action action, CloudService service,
			Map<String, Transition> transitions, LifeCycleEvent event) throws Exception {
		process(event, false);
	}

	@Override
	public void onCustomEvent(String serviceId, String groupId, String eventName, String epsId, String optionalMessage,
			Map<String, Transition> transitions, CustomEvent event) throws Exception {
		process(event, false);
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg) throws Exception {

		if (event.getEventId().equals(msg.getEventCauseId())) {

			if (ComotIllegalArgumentException.class.getName().equals(msg.getType())) {
				exception = new ComotIllegalArgumentException(msg.getMessage());

			} else if (msg instanceof ExceptionMessageLifeCycle) {
				exception = new ComotLifecycleException(((ExceptionMessageLifeCycle) msg).getMessage());

			} else {
				exception = new Exception(msg.getMessage());
			}

			signal.result = false;
		}
	}

	public abstract void process(AbstractEvent event, boolean exception);

	public void send() throws Exception {

		if (event instanceof LifeCycleEvent) {
			coordinator.sendLifeCycle((LifeCycleEvent) event);
		} else {
			coordinator.sendCustom((CustomEvent) event);
		}

		long count = 0;

		while (signal.result == null && count < Coordinator.TIMEOUT) {
			Thread.sleep(100);
			count = count + 100;
		}

		manager.stop();

		if (signal.result == null) {
			throw new ComotException("Timeout waiting for event");
		} else if (signal.result) {
			return;
		} else {
			throw exception;
		}

	}

	protected void clean() {
		manager.stop();
	}

	public class Signal {
		public Boolean result = null;
	}
}
