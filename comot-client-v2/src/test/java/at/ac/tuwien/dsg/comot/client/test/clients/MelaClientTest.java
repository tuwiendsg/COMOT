package at.ac.tuwien.dsg.comot.client.test.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.client.clients.MelaClient;
import at.ac.tuwien.dsg.comot.client.clients.SalsaClient;
import at.ac.tuwien.dsg.comot.client.test.AbstractTest;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.core.test.TestUtils;
import at.ac.tuwien.dsg.comot.core.test.samples.ExampleDeployOneVM;
import at.ac.tuwien.dsg.comot.core.test.samples.ExampleExecutableOnVM;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshots;

public class MelaClientTest extends AbstractTest {

	private static final String SALSA_IP = "128.130.172.215";

	private MelaClient mela;
	private SalsaClient salsa;
	private CloudService serviceTemplate;
	private String serviceId;

	private MonitoredElement eService;
	private MonitoredElement eTopo;
	private MonitoredElement eUnit;
	private MonitoredElement eVM;

	@Before
	public void setup() {

		salsa = new SalsaClient(SALSA_IP);
		mela = new MelaClient("128.130.172.216", 8180);

		serviceTemplate = ExampleExecutableOnVM.build();
		serviceId = serviceTemplate.getId();

		// set up MonitoredElement
		eVM = new MonitoredElement("10.99.0.25");
		eVM.setLevel(MonitoredElement.MonitoredElementLevel.VM);

		eUnit = new MonitoredElement(ExampleDeployOneVM.NODE_ID);
		eUnit.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_UNIT);
		eUnit.addElement(eVM);

		eTopo = new MonitoredElement(ExampleDeployOneVM.TOPOLOGY_ID);
		eTopo.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_TOPOLOGY);
		eTopo.addElement(eUnit);

		eService = new MonitoredElement(serviceId);
		eService.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE);
		eService.addElement(eTopo);
	}

	@After
	public void cleanUp() {
		mela.close();
		salsa.close();
	}

	@Test
	public void helperDeploy() throws CoreServiceException {
		salsa.deploy(serviceTemplate);
	}

	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException, JAXBException, FileNotFoundException {

		log.info(Utils.xmlObjToString(eService));

		// service description
		mela.sendServiceDescription(eService);

		MonitoredElement returned = mela.getServiceDescription(serviceId);
		log.info("getServiceDescription " + Utils.xmlObjToString(returned));
		assertEquals(eService.getId(), returned.getId());

		// update
		updateServiceDescription();

		// list all services
		List<String> list = mela.listAllServices();
		assertEquals(1, list.size());
		assertEquals(serviceId, list.get(0));

		// MCR
		updateMCR();

		// TODO what are requirements?
		// sendRequirements
		
		removeService();
	}

	@Test
	public void testMonitoringData() throws CoreServiceException, InterruptedException, JAXBException {

		MonitoredElementMonitoringSnapshot data;

		data = mela.getMonitoringData(serviceId);
		assertNotNull(data);
		log.info("getMonitoringData(serviceId) \n" + Utils.xmlObjToString(data));

		data = mela.getMonitoringData(serviceId, eVM);
		assertNotNull(data);
		log.info("getMonitoringData(serviceId, eVM) \n" + Utils.xmlObjToString(data));

		MonitoredElementMonitoringSnapshots dataMultiple = mela.getAllAggregatedMonitoringData(serviceId);
		assertNotNull(dataMultiple);
		log.info("getAllAggregatedMonitoringData \n" + Utils.xmlObjToString(dataMultiple));

		// TODO resolve timestamp, why int?
		// dataMultiple = mela.getAllAggregatedMonitoringDataInTimeInterval(serviceId, startTimestamp, endTimestamp);
		// assertNotNull(dataMultiple);
		// log.info("getAllAggregatedMonitoringDataInTimeInterval \n" + Utils.xmlObjToString(dataMultiple));

		dataMultiple = mela.getLastXAggregatedMonitoringData(serviceId, 5);
		assertNotNull(dataMultiple);
		log.info("getLastXAggregatedMonitoringData \n" + Utils.xmlObjToString(dataMultiple));
	}

	@Test
	public void updateServiceDescription() throws CoreServiceException, JAXBException {

		// add one topology
		String newTopoId = ExampleDeployOneVM.TOPOLOGY_ID + "_new";
		MonitoredElement newTopo = new MonitoredElement(newTopoId);
		newTopo.setLevel(MonitoredElement.MonitoredElementLevel.SERVICE_TOPOLOGY);
		newTopo.addElement(eUnit);
		eService.addElement(newTopo);

		// update
		mela.updateServiceDescription(serviceId, eService);

		MonitoredElement returned = mela.getServiceDescription(serviceId);
		log.info("updated  " + Utils.xmlObjToString(returned));
		assertEquals(2, returned.getContainedElements().size());

		// revert update
		eService.removeElement(newTopo);
		mela.updateServiceDescription(serviceId, eService);
		
		returned = mela.getServiceDescription(serviceId);
		log.info("reverted  " + Utils.xmlObjToString(returned));
	}

	@Test
	public void updateMCR() throws CoreServiceException, InterruptedException, JAXBException, FileNotFoundException {

		CompositionRulesConfiguration mcr = mela.getMetricsCompositionRules(serviceId);
		log.info("old MCR \n" + Utils.xmlObjToString(mcr));
		assertNotNull(mcr);

		mela.sendMetricsCompositionRules(serviceId,
				TestUtils.loadMetricCompositionRules(serviceId, "./mela/defCompositionRules.xml"));

		mcr = mela.getMetricsCompositionRules(serviceId);
		log.info("new MCR \n" + Utils.xmlObjToString(mcr));
		assertNotNull(mcr);

	}

	@Test
	public void removeService() throws CoreServiceException, InterruptedException, JAXBException {
		mela.removeServiceDescription(serviceId);
		
		List<String> list = mela.listAllServices();
		assertEquals(0, list.size());
	}

}
