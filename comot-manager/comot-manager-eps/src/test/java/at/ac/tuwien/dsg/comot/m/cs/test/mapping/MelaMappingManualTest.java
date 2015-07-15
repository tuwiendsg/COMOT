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

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsT;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.MelaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.MelaOutputMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class MelaMappingManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(MelaMappingManualTest.class);

	@Autowired
	protected MelaOrika orika;
	@Autowired
	protected MelaMapper mapper;

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;
	@Autowired
	protected MelaOutputMapper mapperMelaOutput;

	protected static final String TEST_SERVICE_ID = "example_executableOnVM_1";

	protected CloudService serviceForMapping;

	@Before
	public void startup() {
		serviceForMapping = STemplates.fullService();
	}

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException, EpsException,
			ComotException {

		// LOG.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		LOG.info("enriched {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		mapperDepl.enrichModel(TEST_SERVICE_ID, service, serviceState);

		// LOG.info("enriched {}", Utils.asJsonString(service));

		MonitoredElement element = mapper.extractMela(service);
		LOG.info("mela {}", UtilsCs.asString(element));

	}

	@Test
	public void requirementsTest() throws JAXBException, ClassNotFoundException, IOException {

		// LOG.info("original {}", Utils.asJsonString(serviceForMapping));

		Requirements element = mapper.extractRequirements(serviceForMapping);
		LOG.info("mela {}", Utils.asXmlString(element));

	}

	@Test
	public void testMelaOutputOffline() throws JAXBException, ClassNotFoundException, IOException,
			EpsException,
			ComotException {

		String melaData = Utils.loadFileFromSystemAsString(UtilsT.TEST_FILE_BASE
				+ "xml/ViennaChillerSensors_monitoringData.xml");

		StringReader reader = new StringReader(melaData);
		JAXBContext jaxbContext = JAXBContext.newInstance(MonitoredElementMonitoringSnapshot.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		MonitoredElementMonitoringSnapshot def = (MonitoredElementMonitoringSnapshot) jaxbUnmarshaller
				.unmarshal(reader);

		ElementMonitoring element = mapperMelaOutput.extractOutput(def);

		LOG.info("mela {}", Utils.asXmlString(element));
	}

}
