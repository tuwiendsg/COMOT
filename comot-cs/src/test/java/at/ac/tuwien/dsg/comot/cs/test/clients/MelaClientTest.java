package at.ac.tuwien.dsg.comot.cs.test.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshots;

public class MelaClientTest extends AbstractTest {

	public static final String SERVICE_ID = "example_executableOnVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	private static final String NODE_IP = "10.99.0.25"; // <== check this

	@Autowired
	private MelaClient mela;
	@Autowired
	private SalsaClient salsa;

	private String xmlTosca;

	private MonitoredElement eService;
	private MonitoredElement eTopo;
	private MonitoredElement eUnit;
	private MonitoredElement eVM;

	@Before
	public void setup() throws IOException {

		xmlTosca = UtilsTest.loadFile("./xml/ExampleExecutableOnVM.xml");

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
	public void helperDeploy() throws CoreServiceException {
		salsa.deploy(xmlTosca);
	}

	// Prerequisites:
	// 1. ExampleExecutableOnVM.xml is deployed to SALSA
	// 2. NODE_IP is correct
	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException, JAXBException, FileNotFoundException {

		log.info(Utils.asXmlString(eService));

		// service description
		mela.sendServiceDescription(eService);

		MonitoredElement returned = mela.getServiceDescription(SERVICE_ID);
		log.info("getServiceDescription " + Utils.asXmlString(returned));
		assertEquals(eService.getId(), returned.getId());

		// update
		updateServiceDescription();

		// list all services
		List<String> list = mela.listAllServices();
		assertEquals(1, list.size());
		assertEquals(SERVICE_ID, list.get(0));

		// MCR
		updateMCR();

		// TODO what are requirements?
		// sendRequirements

		removeService();

	}

	@Test
	public void testMonitoringData() throws CoreServiceException, InterruptedException, JAXBException {

		MonitoredElementMonitoringSnapshot data;

		data = mela.getMonitoringData(SERVICE_ID);
		assertNotNull(data);
		log.info("getMonitoringData(SERVICE_ID) \n" + Utils.asXmlString(data));

		data = mela.getMonitoringData(SERVICE_ID, eVM);
		assertNotNull(data);
		log.info("getMonitoringData(SERVICE_ID, eVM) \n" + Utils.asXmlString(data));

		MonitoredElementMonitoringSnapshots dataMultiple = mela.getAllAggregatedMonitoringData(SERVICE_ID);
		assertNotNull(dataMultiple);
		log.info("getAllAggregatedMonitoringData \n" + Utils.asXmlString(dataMultiple));

		// dataMultiple = mela.getAllAggregatedMonitoringDataInTimeInterval(SERVICE_ID, startTimestamp, endTimestamp);
		// assertNotNull(dataMultiple);
		// log.info("getAllAggregatedMonitoringDataInTimeInterval \n" + Utils.xmlObjToString(dataMultiple));

		dataMultiple = mela.getLastXAggregatedMonitoringData(SERVICE_ID, 5);
		assertNotNull(dataMultiple);
		log.info("getLastXAggregatedMonitoringData \n" + Utils.asXmlString(dataMultiple));
	}

	@Test
	public void updateServiceDescription() throws CoreServiceException, JAXBException {

		// add one topology
		String newTopoId = TOPOLOGY_ID + "_new";
		MonitoredElement newTopo = new MonitoredElement(newTopoId);
		newTopo.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_TOPOLOGY);
		newTopo.addElement(eUnit);
		eService.addElement(newTopo);

		// update
		mela.updateServiceDescription(SERVICE_ID, eService);

		MonitoredElement returned = mela.getServiceDescription(SERVICE_ID);
		log.info("updated  " + Utils.asXmlString(returned));
		assertEquals(2, returned.getContainedElements().size());

		// revert update
		eService.removeElement(newTopo);
		mela.updateServiceDescription(SERVICE_ID, eService);

		returned = mela.getServiceDescription(SERVICE_ID);
		log.info("reverted  " + Utils.asXmlString(returned));
	}

	@Test
	public void updateMCR() throws CoreServiceException, InterruptedException, JAXBException, FileNotFoundException {

		CompositionRulesConfiguration mcr = mela.getMetricsCompositionRules(SERVICE_ID);
		log.info("old MCR \n" + Utils.asXmlString(mcr));
		assertNotNull(mcr);

		mela.sendMetricsCompositionRules(SERVICE_ID,
				UtilsTest.loadMetricCompositionRules(SERVICE_ID, "./mela/defCompositionRules.xml"));

		mcr = mela.getMetricsCompositionRules(SERVICE_ID);
		log.info("new MCR \n" + Utils.asXmlString(mcr));
		assertNotNull(mcr);

	}

	@Test
	public void removeService() throws CoreServiceException, InterruptedException, JAXBException {
		mela.removeServiceDescription(SERVICE_ID);

		List<String> list = mela.listAllServices();
		assertEquals(0, list.size());
	}

}
