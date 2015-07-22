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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.m.core;

import java.io.File;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.ConfigConstants;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.EpsAdapterExternal;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.InfoServiceUtils;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;

@Component
public class Coordinator {

	private static final Logger LOG = LoggerFactory.getLogger(Coordinator.class);

	public static final String USER_ID = "Some_User";
	public static final long TIMEOUT = 60000;
	public static final String FILE_SUFFIX = ".tar.gz";
	public static final String UPLOADS_DIR = "temp/";
	public static final String ADAPTER_ID = "adapter";

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InfoClient infoService;
	@Autowired
	protected RabbitTemplate amqp;

	@Autowired
	protected ToscaMapper mapperTosca;
	@javax.annotation.Resource
	public Environment env;

	public String createService(CloudService service) throws Exception {

		String serviceId = infoService.createService(service);
		LOG.info("serviceId {}", serviceId);
		sendAndWaitForId(new LifeCycleEventModifying(serviceId, serviceId, Action.CREATED, null, service));

		return serviceId;

	}

	public String createServiceFromTemplate(String templateId) throws Exception {

		String serviceId = infoService.createServiceFromTemplate(templateId);
		CloudService service = infoService.getService(serviceId);

		sendAndWaitForId(new LifeCycleEventModifying(serviceId, serviceId, Action.CREATED, null, service));

		return serviceId;
	}

	public void startService(String serviceId) throws Exception {

		sendAndWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.START));
	}

	public void stopService(String serviceId) throws Exception {

		sendAndWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.STOP));
	}

	public void removeService(String serviceId) throws Exception {

		sendAndWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.REMOVED));
	}

	public void reconfigureElasticity(String serviceId, CloudService service) throws Exception {

		sendAndWaitForId(new LifeCycleEventModifying(serviceId, serviceId,
				Action.RECONFIGURE_ELASTICITY, null, service));
	}

	public void kill(String serviceId) throws Exception {

		sendAndWaitForId(new LifeCycleEvent(serviceId, serviceId, Action.TERMINATE));
	}

	public String addStaticEps(OfferedServiceUnit osu) throws NumberFormatException, Exception {

		if (!InfoServiceUtils.isStaticEps(osu)) {
			throw new ComotIllegalArgumentException("The OfferedServiceUnit is not a valid external EPS");
		}

		String epsId = infoService.addOsu(osu);

		Class<?> clazz = null;
		String ip = null;
		String port = null;

		for (Resource res : osu.getResources()) {
			switch (res.getType().getName()) {
			case Constants.ADAPTER_CLASS:
				clazz = Class.forName(res.getName());
				break;
			case Constants.IP:
				ip = res.getName();
				break;
			case Constants.PORT:
				port = res.getName();
				break;
			}
		}

		String epsInstanceId = infoService.createOsuInstance(osu.getId());

		if (clazz != null) {
			EpsAdapterExternal adapter = (EpsAdapterExternal) context.getBean(clazz);
			adapter.start(epsInstanceId, ip, (port != null) ? Integer.valueOf(port) : null);
		}

		return epsId;
	}

	public void assignSupportingOsu(String serviceId, String osuInstanceId)
			throws Exception {

		sendAndWaitCorrelationId(new CustomEvent(serviceId, serviceId, EpsEvent.EPS_SUPPORT_REQUESTED.toString(),
				osuInstanceId, null));
	}

	public void removeAssignmentOfSupportingOsu(String serviceId, String osuInstanceId)
			throws Exception {

		sendAndWaitForId(new CustomEvent(serviceId, serviceId, EpsEvent.EPS_SUPPORT_REMOVED.toString(),
				osuInstanceId, null));
	}

	public String createDynamicEps(String epsId, File file) throws Exception {

		String epsInstanceId = infoService.createOsuInstance(epsId);

		if (file != null) {

			String user = env.getProperty(ConfigConstants.REPO_USERNAME);
			String host = env.getProperty(ConfigConstants.REPO_HOST);
			String pem = env.getProperty(ConfigConstants.RESOURCE_PATH) + env.getProperty(ConfigConstants.REPO_PEM);
			String rPath = env.getProperty(ConfigConstants.REPO_PATH);
			String rFile = UUID.randomUUID() + FILE_SUFFIX;

			UtilsFile.upload(file, host, rPath + rFile, user, new File(pem));

			CloudService service = infoService.getOsuInstance(epsInstanceId).getService();
			insertConfigToTosca(service, rFile);
			infoService.updateService(service);
		}

		sendCustom(new CustomEvent(null, null, EpsEvent.EPS_DYNAMIC_REQUESTED.toString(), null, epsInstanceId));

		return epsInstanceId;
	}

	protected void insertConfigToTosca(CloudService service, String rFile) throws EpsException {

		Navigator nav = new Navigator(service);

		for (ServiceUnit unit : nav.getAllUnits()) {
			for (ConnectToRel rel : unit.getConnectTo()) {
				if (ADAPTER_ID.equals(rel.getTo().getId())) {

					Resource url = new Resource(
							env.getProperty(ConfigConstants.REPO_URL) + rFile,
							new ResourceOrQualityType(ResourceOrQualityType.ART_REFERENCE_TYPE));
					Resource tarGz = new Resource("configuration", new ResourceOrQualityType("misc"));
					tarGz.hasResource(url);
					unit.getOsuInstance().getOsu().hasResource(tarGz);

					return;
				}
			}
		}
	}

	public void removeDynamicEps(String epsId, String epsInstanceId) throws Exception {

		OsuInstance osuInatance = infoService.getOsuInstance(epsInstanceId);

		String serviceId = osuInatance.getService().getId();

		sendAndWaitForId(new CustomEvent(serviceId, serviceId, EpsEvent.EPS_DYNAMIC_REMOVED.toString(),
				Constants.EPS_BUILDER, epsInstanceId));

	}

	public void triggerCustomEvent(
			String serviceId,
			String epsId,
			String eventId,
			String optionalInput) throws Exception {

		if (StringUtils.isBlank(eventId)) {
			return;
		}

		sendAndWaitForId(new CustomEvent(serviceId, serviceId, eventId, epsId, optionalInput));

	}

	protected void sendAndWaitForId(final AbstractEvent event) throws Exception {

		final String evantId = event.getEventId();

		CoordinatorAdapter processor = new CoordinatorAdapter(event, this, context) {
			@Override
			public void process(AbstractEvent event, boolean exception) {
				if (event.getEventId().equals(evantId)) {
					signal.result = !exception;
				}
			}
		};

		processor.send();
	}

	protected void sendAndWaitCorrelationId(final AbstractEvent event) throws Exception {

		final String evantId = event.getEventId();

		CoordinatorAdapter processor = new CoordinatorAdapter(event, this, context) {
			@Override
			public void process(AbstractEvent event, boolean exception) {

				if (event.getCorrelationId().equals(evantId)) {
					signal.result = !exception;
				}
			}
		};

		processor.send();
	}

	protected void sendLifeCycle(LifeCycleEvent event) throws JAXBException {

		String bindingKey = event.getServiceId() + "." + LifeCycleEvent.class.getSimpleName() + "." + event.getAction()
				+ "." + event.getGroupId();

		// LOG.info(logId() +"SEND key={}", targetLevel);

		event.setOrigin(USER_ID);
		event.setTime(System.currentTimeMillis());

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	protected void sendCustom(CustomEvent event) throws JAXBException {

		String bindingKey = event.getServiceId() + "." + CustomEvent.class.getSimpleName() + "."
				+ event.getCustomEvent() + "." + event.getGroupId();

		// LOG.info(logId() +"SEND key={}", targetLevel);
		event.setOrigin(USER_ID);
		event.setTime(System.currentTimeMillis());

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

}
