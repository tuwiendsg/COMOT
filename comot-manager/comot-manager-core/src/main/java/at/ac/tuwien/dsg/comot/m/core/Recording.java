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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessageLifeCycle;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.ConverterToInternal;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.CustomReflectionUtils;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

@Component
public class Recording extends Processor {

	private static final Logger LOG = LoggerFactory.getLogger(Recording.class);

	public static final String CHANGE_TYPE_LIFECYCLE = "CHANGE_TYPE_LIFECYCLE";
	public static final String CHANGE_TYPE_CUSTOM = "CHANGE_TYPE_CUSTOM";
	public static final String CHANGE_TYPE_EXCEPTION = "CHANGE_TYPE_EXCEPTION";
	public static final String CHANGE_TYPE_EXCEPTION_LIFECYCLE = "CHANGE_TYPE_EXCEPTION_LIFECYCLE";

	public static final String PROP_ORIGIN = "origin";
	public static final String PROP_MSG = "optionalMessage";
	public static final String PROP_EPS_ID = "epsId";
	public static final String PROP_EVENT_NAME = "eventName";
	public static final String PROP_EVENT_TIME = "eventTime";
	public static final String PROP_EXCEPTION_TYPE = "exceptionType";
	public static final String PROP_EXCEPTION_MSG = "exceptionMsg";
	public static final String PROP_EXCEPTION_DETAIL = "exceptionDetail";
	public static final String PROP_EVENT = "eventProperty-";

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected RevisionApi revisionApi;

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {
		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName, "#"));
		bindings.add(bindingCustom(queueName, "#"));
		bindings.add(bindingException(queueName, "#"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String groupId,
			Action action, String originId, CloudService service, Map<String, Transition> transitions)
			throws JAXBException, IllegalAccessException {

		LifeCycleEvent event = (LifeCycleEvent) msg.getEvent();

		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put(PROP_ORIGIN, originId);
		changeProperties.put(PROP_EVENT_NAME, action.toString());
		changeProperties.put(PROP_EVENT_TIME, event.getTime());

		UtilsLc.removeProviderInfoExceptType(service);

		revisionApi.createOrUpdateRegion(service, serviceId, groupId, CHANGE_TYPE_LIFECYCLE, changeProperties);

	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String groupId,
			String event, String epsId, String originId, String optionalMessage) {

		CustomEvent eventMsg = (CustomEvent) msg.getEvent();

		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put(PROP_ORIGIN, originId);
		changeProperties.put(PROP_EVENT_NAME, event);
		changeProperties.put(PROP_EVENT_TIME, eventMsg.getTime());

		if (epsId != null) {
			changeProperties.put(PROP_EPS_ID, epsId);
		}
		if (optionalMessage != null) {
			changeProperties.put(PROP_MSG, optionalMessage);
		}

		if (revisionApi.verifyObject(serviceId, serviceId)) {
			revisionApi.storeEvent(serviceId, groupId, CHANGE_TYPE_CUSTOM, changeProperties);
		} else {
			LOG.error("Custom event happened, but no managed region. {}", msg.getEvent());
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String originId) throws Exception {

		String type;
		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put(PROP_ORIGIN, originId);
		changeProperties.put(PROP_EVENT_TIME, msg.getTime());
		changeProperties.put(PROP_EVENT_NAME, msg.getType());
		changeProperties.put(PROP_EXCEPTION_TYPE, msg.getType());
		changeProperties.put(PROP_EXCEPTION_MSG, msg.getMessage());
		changeProperties.put(PROP_EXCEPTION_DETAIL, msg.getDetails());

		if (msg instanceof ExceptionMessageLifeCycle) {
			ExceptionMessageLifeCycle lcMsg = (ExceptionMessageLifeCycle) msg;
			Map<String, Object> eventProps = ConverterToInternal.extractProperties(lcMsg.getEvent(),
					CustomReflectionUtils.getInheritedNonStaticNonTransientNonNullFields(lcMsg.getEvent()));

			for (String key : eventProps.keySet()) {
				changeProperties.put(PROP_EVENT + key, eventProps.get(key));
			}

			type = CHANGE_TYPE_EXCEPTION_LIFECYCLE;
		} else {
			type = CHANGE_TYPE_EXCEPTION;
		}

		if (revisionApi.verifyObject(serviceId, serviceId)) {
			revisionApi.storeEvent(serviceId, serviceId, type, changeProperties);
		} else {
			LOG.error("Exception event happened, but no managed region. {}", msg);
		}

	}

	public static Long extractEventTime(Change change) {
		return (Long) change.getProperty(PROP_EVENT_TIME);
	}

	public static String extractEventName(Change change) {
		return change.getProperty(PROP_EVENT_NAME).toString();
	}

}
