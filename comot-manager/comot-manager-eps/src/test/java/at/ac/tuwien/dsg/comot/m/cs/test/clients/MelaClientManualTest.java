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
package at.ac.tuwien.dsg.comot.m.cs.test.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshots;

public class MelaClientManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(MelaClientManualTest.class);

	public static final String SERVICE_ID = "example_executableOnVM";// "example_executableOnVM"; //comot_tomcat_id
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	private static final String NODE_IP = "10.99.0.26"; // <== check this

	@Autowired
	private MelaClient mela;
	@Autowired
	private SalsaClient salsa;

	private MonitoredElement eService;
	private MonitoredElement eTopo;
	private MonitoredElement eUnit;
	private MonitoredElement eVM;

	@Before
	public void setup() {

		// set up MonitoredElement
		eVM = new MonitoredElement(NODE_IP);
		eVM.setLevel(MonitoredElement.MonitoredElementLevel.VM);

		eUnit = new MonitoredElement(NODE_ID);
		eUnit.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_UNIT);
		eUnit.addElement(eVM);

		eTopo = new MonitoredElement(TOPOLOGY_ID);
		eTopo.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_TOPOLOGY);
		eTopo.addElement(eUnit);

		eService = new MonitoredElement(SERVICE_ID);
		eService.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE);
		eService.addElement(eTopo);
	}

	@Test
	public void helperDeploy() throws EpsException, IOException {

		String xmlTosca = Utils.loadFileFromSystemAsString("./../resources/test/xml/ExampleExecutableOnVM.xml");
		salsa.deploy(xmlTosca);
	}

	// Prerequisites:
	// 1. ExampleExecutableOnVM.xml is deployed to SALSA
	// 2. NODE_IP is correct
	@Test
	public void testAutomated() throws EpsException, InterruptedException, JAXBException, IOException,
			URISyntaxException {

		LOG.info(Utils.asXmlString(eService));

		// service description
		testStartMonitoring();

		// update
		updateServiceDescription();

		// list all services
		List<String> list = mela.listAllServices();
		assertEquals(1, list.size());
		assertEquals(SERVICE_ID, list.get(0));

		// MCR
		updateMCR();

		// sendRequirements
		removeService();

	}

	@Test
	public void testStartMonitoring() throws EpsException, JAXBException {
		LOG.info(Utils.asXmlString(eService));

		mela.sendServiceDescription(eService);

		MonitoredElement returned = mela.getServiceDescription(SERVICE_ID);
		LOG.info("getServiceDescription " + Utils.asXmlString(returned));
		assertEquals(eService.getId(), returned.getId());
	}

	@Test
	public void testMonitoringData() throws EpsException, InterruptedException, JAXBException {

		MonitoredElementMonitoringSnapshot data;

		data = mela.getMonitoringData(SERVICE_ID);
		assertNotNull(data);
		LOG.info("getMonitoringData(SERVICE_ID) \n" + Utils.asXmlString(data));

		data = mela.getMonitoringData(SERVICE_ID, eVM);
		assertNotNull(data);
		LOG.info("getMonitoringData(SERVICE_ID, eVM) \n" + Utils.asXmlString(data));

		MonitoredElementMonitoringSnapshots dataMultiple = mela.getAllAggregatedMonitoringData(SERVICE_ID);
		assertNotNull(dataMultiple);
		assertTrue(0 < dataMultiple.getChildren().size());
		LOG.info("getAllAggregatedMonitoringData \n" + Utils.asXmlString(dataMultiple));

		// dataMultiple = mela.getAllAggregatedMonitoringDataInTimeInterval(SERVICE_ID, startTimestamp, endTimestamp);
		// assertNotNull(dataMultiple);
		// LOG.info("getAllAggregatedMonitoringDataInTimeInterval \n" + Utils.xmlObjToString(dataMultiple));

		dataMultiple = mela.getLastXAggregatedMonitoringData(SERVICE_ID, 5);
		assertNotNull(dataMultiple);
		assertTrue(0 < dataMultiple.getChildren().size());
		LOG.info("getLastXAggregatedMonitoringData \n" + Utils.asXmlString(dataMultiple));
	}

	@Test
	public void updateServiceDescription() throws EpsException, JAXBException {

		// add one topology
		String newTopoId = TOPOLOGY_ID + "_new";
		MonitoredElement newTopo = new MonitoredElement(newTopoId);
		newTopo.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_TOPOLOGY);
		newTopo.addElement(eUnit);
		eService.addElement(newTopo);

		// update
		mela.updateServiceDescription(SERVICE_ID, eService);

		MonitoredElement returned = mela.getServiceDescription(SERVICE_ID);
		LOG.info("updated  " + Utils.asXmlString(returned));
		assertEquals(2, returned.getContainedElements().size());

		// revert update
		eService.removeElement(newTopo);
		mela.updateServiceDescription(SERVICE_ID, eService);

		returned = mela.getServiceDescription(SERVICE_ID);
		LOG.info("reverted  " + Utils.asXmlString(returned));
	}

	@Test
	public void updateMCR() throws EpsException, InterruptedException, JAXBException, IOException {

		CompositionRulesConfiguration mcr = mela.getMetricsCompositionRules(SERVICE_ID);
		LOG.info("old MCR \n" + Utils.asXmlString(mcr));
		assertNotNull(mcr);

		mela.sendMetricsCompositionRules(SERVICE_ID,
				UtilsCs.loadMetricCompositionRules(SERVICE_ID, "./../resources/test/mela/defCompositionRules.xml"));

		mcr = mela.getMetricsCompositionRules(SERVICE_ID);
		LOG.info("new MCR \n" + Utils.asXmlString(mcr));
		assertNotNull(mcr);

	}

	@Test
	public void removeService() throws EpsException, InterruptedException, JAXBException, URISyntaxException {
		mela.setBaseUri(new URI("http://localhost:8180/MELA/REST_WS"));
		// mela.removeServiceDescription("HelloElasticityNoDB"); ExampleExecutableOnVM
		mela.removeServiceDescription("HelloElasticityNoDB_co_1");

		List<String> list = mela.listAllServices();
		assertEquals(0, list.size());
	}

}
