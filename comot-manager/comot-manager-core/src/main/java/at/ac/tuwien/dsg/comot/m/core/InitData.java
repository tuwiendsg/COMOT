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
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.ComotEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotAdapterException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.core.adapter.ControlAdapterStatic;
import at.ac.tuwien.dsg.comot.m.core.adapter.DeploymentAdapterStatic;
import at.ac.tuwien.dsg.comot.m.core.adapter.MonitoringAdapterStatic;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.ComotCustomEvent;
import at.ac.tuwien.dsg.comot.model.provider.ComotLifecycleEvent;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

@Component
public class InitData {

	private static final Logger LOG = LoggerFactory.getLogger(InitData.class);

	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected ToscaMapper mapperTosca;
	@javax.annotation.Resource
	public Environment env;

	public void setUpTestData() throws ComotException {

		String fileBase = env.getProperty("dir.files");

		URI deploymentUri;
		URI monitoringUri;
		URI controllerUri;
		try {
			deploymentUri = new URI(env.getProperty("uri.deployemnt"));
			monitoringUri = new URI(env.getProperty("uri.monitoring"));
			controllerUri = new URI(env.getProperty("uri.controller"));
		} catch (URISyntaxException e1) {
			throw new ComotAdapterException(e1);
		}

		// SALSA

		OfferedServiceUnit deployment = new OfferedServiceUnit(
				Constants.SALSA_SERVICE_STATIC, Constants.SALSA_SERVICE_STATIC, OsuType.EPS.toString(),
				new String[] { Constants.ROLE_DEPLOYER });
		deployment.hasResource(new Resource(DeploymentAdapterStatic.class.getCanonicalName(),
				new ResourceOrQualityType(Constants.ADAPTER_CLASS)));
		deployment.hasResource(new Resource(deploymentUri.getHost(),
				new ResourceOrQualityType(Constants.IP)));
		deployment.hasResource(new Resource("" + deploymentUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		deployment.hasResource(new Resource(
				"/salsa-engine?id=" + Constants.PLACE_HOLDER_INSTANCE_ID,
				new ResourceOrQualityType(Constants.VIEW)));

		// MELA
		OfferedServiceUnit monitoring = new OfferedServiceUnit(
				Constants.MELA_SERVICE_STATIC, Constants.MELA_SERVICE_STATIC, OsuType.EPS.toString(),
				new String[] { Constants.ROLE_OBSERVER });
		monitoring.hasResource(new Resource(MonitoringAdapterStatic.class.getCanonicalName(),
				new ResourceOrQualityType(Constants.ADAPTER_CLASS)));
		monitoring.hasResource(new Resource(monitoringUri.getHost(), new ResourceOrQualityType(
				Constants.IP)));
		monitoring.hasResource(new Resource("" + monitoringUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		monitoring.hasResource(new Resource(
				"/MELA/mela.html?" + Constants.PLACE_HOLDER_INSTANCE_ID,
				new ResourceOrQualityType(Constants.VIEW)));

		monitoring.hasPrimitiveOperation(
				new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
		monitoring.hasPrimitiveOperation(
				new ComotCustomEvent("Start monitoring", ComotEvent.MELA_START.toString(), false));
		monitoring.hasPrimitiveOperation(
				new ComotCustomEvent("Stop monitoring", ComotEvent.MELA_STOP.toString(), false));

		// RSYBL

		OfferedServiceUnit control = new OfferedServiceUnit(
				Constants.RSYBL_SERVICE_STATIC, Constants.RSYBL_SERVICE_STATIC, OsuType.EPS.toString(),
				new String[] { Constants.ROLE_CONTROLLER });
		control.hasResource(new Resource(ControlAdapterStatic.class.getCanonicalName(), new ResourceOrQualityType(
				Constants.ADAPTER_CLASS)));
		control.hasResource(new Resource(controllerUri.getHost(), new ResourceOrQualityType(Constants.IP)));
		control.hasResource(new Resource("" + controllerUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		control.hasResource(new Resource(
				"/rSYBL/",
				new ResourceOrQualityType(Constants.VIEW)));

		control.hasPrimitiveOperation(
				new ComotLifecycleEvent("Stop controller", Action.STOP_CONTROLLER.toString()));

		control.hasPrimitiveOperation(
				new ComotCustomEvent("Set Control Metrics", ComotEvent.SET_MCR.toString(), true));
		control.hasPrimitiveOperation(
				new ComotCustomEvent("Start controller", ComotEvent.RSYBL_START.toString(), false));
		control.hasPrimitiveOperation(
				new ComotCustomEvent("Stop controller", ComotEvent.RSYBL_STOP.toString(), false));

		infoService.addOsu(deployment);
		infoService.addOsu(monitoring);
		infoService.addOsu(control);

		// DYNAMIC EPS MELA

		try {

			CloudService melaService = mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "adapterMela/mela_tosca_with_adapter_from_salsa.xml"));

			OfferedServiceUnit monitoringDynamic = new OfferedServiceUnit(
					Constants.MELA_SERVICE_DYNAMIC, Constants.MELA_SERVICE_DYNAMIC, OsuType.EPS.toString(),
					new String[] { Constants.ROLE_OBSERVER });

			monitoringDynamic.hasPrimitiveOperation(
					new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
			monitoringDynamic.hasPrimitiveOperation(
					new ComotCustomEvent("Start monitoring", ComotEvent.MELA_START.toString(), false));
			monitoringDynamic.hasPrimitiveOperation(
					new ComotCustomEvent("Stop monitoring", ComotEvent.MELA_STOP.toString(), false));

			monitoringDynamic.setServiceTemplate(new Template(melaService.getId(), melaService));

			infoService.addOsu(monitoringDynamic);

		} catch (JAXBException | IOException e) {
			LOG.error("{}", e);
		}

		// DYNAMIC EPS RSYBL
		try {
			CloudService rsyblService = mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "adapterRsybl/rsybl_mela_with_adapter_tosca.xml"));

			OfferedServiceUnit rsyblDynamic = new OfferedServiceUnit(
					Constants.RSYBL_SERVICE_DYNAMIC, Constants.RSYBL_SERVICE_DYNAMIC, OsuType.EPS.toString(),
					new String[] { Constants.ROLE_CONTROLLER });

			rsyblDynamic.hasPrimitiveOperation(
					new ComotLifecycleEvent("Stop controller", Action.STOP_CONTROLLER.toString()));

			rsyblDynamic.hasPrimitiveOperation(
					new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
			rsyblDynamic.hasPrimitiveOperation(
					new ComotCustomEvent("Start control", ComotEvent.RSYBL_START.toString(), false));
			rsyblDynamic.hasPrimitiveOperation(
					new ComotCustomEvent("Stop control", ComotEvent.RSYBL_STOP.toString(), false));

			rsyblDynamic.setServiceTemplate(new Template(rsyblService.getId(), rsyblService));

			infoService.addOsu(rsyblDynamic);

		} catch (JAXBException | IOException e) {
			LOG.error("{}", e);
		}

		// DYNAMIC EPS SALSA
		try {
			CloudService salsaService = mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "adapterSalsa/salsa_tosca.xml"));

			OfferedServiceUnit salsaDynamic = new OfferedServiceUnit(
					Constants.SALSA_SERVICE_DYNAMIC, Constants.SALSA_SERVICE_DYNAMIC, OsuType.EPS.toString(),
					new String[] { Constants.ROLE_DEPLOYER });

			salsaDynamic.setServiceTemplate(new Template(salsaService.getId(), salsaService));

			infoService.addOsu(salsaDynamic);

		} catch (JAXBException | IOException e) {
			LOG.error("{}", e);
		}

		try {
			infoService.createTemplate(mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "init/HelloElasticity.xml")));
		} catch (JAXBException | IOException e) {
			LOG.error("{}", e);
		}
	}

}
