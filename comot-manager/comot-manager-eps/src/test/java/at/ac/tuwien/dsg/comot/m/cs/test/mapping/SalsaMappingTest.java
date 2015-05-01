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
package at.ac.tuwien.dsg.comot.m.cs.test.mapping;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.ToscaOrika;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaMappingTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(SalsaMappingTest.class);

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;
	@Autowired
	protected ToscaOrika toscaOrika;

	protected CloudService serviceForMapping;

	// test with https://github.com/tuwiendsg/SALSA/blob/master/examples/4-DeployWithTomcat.xml
	protected static final String TEST_SERVICE_ID = "comot_tomcat_id";

	@Before
	public void startup() {
		serviceForMapping = STemplates.fullServiceWithoutInstances();
	}

	@Test
	public void automatedMapperTest() throws JAXBException {

		LOG.info("original {}", Utils.asXmlString(serviceForMapping));

		Definitions tosca1 = mapperTosca.extractTosca(serviceForMapping);
		LOG.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service2 = mapperTosca.createModel(tosca1);
		LOG.info("service2 {}", Utils.asXmlString(service2));
		assertReflectionEquals(serviceForMapping, service2, ReflectionComparatorMode.LENIENT_ORDER);

		Definitions tosca2 = mapperTosca.extractTosca(service2);
		LOG.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service3 = mapperTosca.createModel(tosca2);
		LOG.info("service3 {}", Utils.asXmlString(service3));
		assertReflectionEquals(serviceForMapping, service3, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void testToscaFromFile() throws JAXBException, IOException {

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/daas_m2m_fromSalsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/tomcat_from_salsa.xml");
		LOG.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service1 = mapperTosca.createModel(tosca1);
		LOG.info("service1 {}", Utils.asXmlString(service1));

		Definitions tosca2 = mapperTosca.extractTosca(service1);
		LOG.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service2 = mapperTosca.createModel(tosca2);
		LOG.info("service2 {}", Utils.asXmlString(service2));
		assertReflectionEquals(service1, service2, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void orikaTest() throws JAXBException {

		LOG.info("original {} \n", Utils.asXmlString(serviceForMapping));

		Definitions tosca1 = toscaOrika.get().map(serviceForMapping, Definitions.class);
		LOG.info("tosca1 {} \n", UtilsCs.asString(tosca1));

		CloudService service2 = toscaOrika.get().map(tosca1, CloudService.class);
		LOG.info("service2 {} \n", Utils.asXmlString(service2));

		Definitions tosca2 = toscaOrika.get().map(service2, Definitions.class);
		LOG.info("tosca2 {} \n", UtilsCs.asString(tosca2));

		CloudService service3 = toscaOrika.get().map(tosca2, CloudService.class);
		LOG.info("service3 {}", Utils.asXmlString(service3));

	}

	@Test
	public void stateMapperTest() throws EpsException, JAXBException, ComotException {

		// ENRICH WITH STATE

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		LOG.info("tosca {}", UtilsCs.asString(def));
		LOG.info("state {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		LOG.info("service {}", Utils.asXmlString(service));

		mapperDepl.enrichModel(TEST_SERVICE_ID, service, serviceState);
		LOG.info("service enriched{}", Utils.asXmlString(service));

		// EXTRACT DEPLOYMENT DESCRIOPTION

		DeploymentDescription descr = mapperDepl.extractDeployment(service);
		LOG.info("depl {}", UtilsCs.asString(descr));

	}

}
