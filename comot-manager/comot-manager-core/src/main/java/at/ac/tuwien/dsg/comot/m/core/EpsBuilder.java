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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.EpsAdapterStatic;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;


@Component
public class EpsBuilder extends Processor {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InformationClient infoService;

	@Override
	public void start() throws Exception {

		infoService.deleteAll();
		context.getBean(InitData.class).setUpTestData();

		// create static EPSes
		for (OfferedServiceUnit osu : infoService.getOsus()) {
			try {

				if (osu.getServiceTemplate() == null) {

					Class<?> clazz = null;
					String ip = null;
					String port = null;

					for (Resource res : osu.getResources()) {
						if (res.getType().getName().equals(Constants.ADAPTER_CLASS)) {
							clazz = Class.forName(res.getName());

						} else if (res.getType().getName().equals(Constants.IP)) {
							ip = res.getName();

						} else if (res.getType().getName().equals(Constants.PORT)) {
							port = res.getName();
						}
					}

					String epsId = infoService.createOsuInstance(osu.getId());
					EpsAdapterStatic adapter = (EpsAdapterStatic) context.getBean(clazz);
					adapter.start(epsId, ip, new Integer(port));

				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {
		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingCustom(queueName, "*.*." + EpsEvent.EPS_DYNAMIC_REQUESTED + "." + Type.SERVICE));
		bindings.add(bindingCustom(queueName, "*.*." + EpsEvent.EPS_DYNAMIC_REMOVED + "." + Type.SERVICE));
		bindings.add(bindingCustom(queueName, "*.*." + EpsEvent.EPS_SUPPORT_ASSIGNED + "." + Type.SERVICE));
		bindings.add(bindingLifeCycle(queueName, "*.*.*.*." + Action.CREATED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName, "*.*.*.*." + Action.UNDEPLOYED + "."+Type.SERVICE+".#"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws ClassNotFoundException, IOException, AmqpException, JAXBException, EpsException {

		if (infoService.isServiceOfDynamicEps(serviceId)) {

			if (action == Action.CREATED) {
				String staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);

				manager.sendCustom(Type.SERVICE, new CustomEvent(serviceId, serviceId,
						EpsEvent.EPS_SUPPORT_REQUESTED.toString(), staticDeplId, null));

			} else if (action == Action.UNDEPLOYED) {

				manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, serviceId, Action.REMOVED));

			}
		}
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String groupId,
			String event, String epsId, String origin, String optionalMessage) throws ClassNotFoundException,
			AmqpException, JAXBException, EpsException {

		EpsEvent action = EpsEvent.valueOf(event);

		if (action == EpsEvent.EPS_DYNAMIC_REQUESTED && !origin.equals(getId())) {

			serviceId = infoService.getOsuInstance(optionalMessage).getService().getId();

			manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, serviceId, Action.CREATED));

		} else if (action == EpsEvent.EPS_SUPPORT_ASSIGNED && infoService.isServiceOfDynamicEps(serviceId)) {

			manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, serviceId, Action.START));

		} else if (action == EpsEvent.EPS_DYNAMIC_REMOVED) {

			manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, serviceId, Action.STOP));

		}

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String originId) throws Exception {
		// TODO Auto-generated method stub

	}

}
