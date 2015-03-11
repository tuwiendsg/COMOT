package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.ComotAction;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class LifecycleStaticEpsTest extends AbstractTest {

	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected Coordinator coordinator;
	@Autowired
	protected InformationServiceMock infoService;

	CloudService service;
	String serviceId;
	String instanceId;
	String monitoringId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException {
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		monitoringId = InformationServiceMock.MELA_SERVICE_PUBLIC_ID;
	}

	@Test
	public void produceEvent() throws JAXBException, IOException, ClassNotFoundException {

		UtilsTest.sleepInfinit();
	}

	@Test
	public void testDeployTomcat() throws IOException, JAXBException, ClassNotFoundException {

		coordinator.startServiceInstance(serviceId, instanceId);
		coordinator.assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

		while (lcManager.getCurrentState(instanceId, serviceId) != State.RUNNING) {
			UtilsTest.sleepSeconds(10);
		}

		coordinator.stopServiceInstance(serviceId, instanceId);

		log.info("{}", Utils.asJsonString(infoService.getService(serviceId)));

		UtilsTest.sleepSeconds(5);
		// UtilsTest.sleepInfinit();
	}

	@Test
	public void testDeployTomcatNormal() throws IOException, JAXBException, ClassNotFoundException {

		coordinator.assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);
		coordinator.startServiceInstance(serviceId, instanceId);

		while (lcManager.getCurrentState(instanceId, serviceId) != State.RUNNING) {
			UtilsTest.sleepSeconds(10);
		}

		coordinator.stopServiceInstance(serviceId, instanceId);

		log.info("{}", Utils.asJsonString(infoService.getService(serviceId)));

		UtilsTest.sleepSeconds(5);
		// UtilsTest.sleepInfinit();

	}

	@Test
	public void testMonitoring() throws IOException, JAXBException, ClassNotFoundException, CoreServiceException {

		State state;

		coordinator.assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);
		coordinator.assignSupportingOsu(serviceId, instanceId, monitoringId);

		coordinator.startServiceInstance(serviceId, instanceId);

		while (lcManager.getCurrentState(instanceId, serviceId) != State.RUNNING) {
			UtilsTest.sleepSeconds(5);

			state = lcManager.getCurrentState(instanceId, serviceId);

			switch (state) {

			case INIT:
			case PASSIVE:
			case STARTING:
			case DEPLOYING:
				assertTrue(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
				assertFalse(isMonitored(instanceId));
				break;

			case RUNNING:
				UtilsTest.sleepSeconds(2);
				assertTrue(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
				assertTrue(isMonitored(instanceId));

				// manually stop
				coordinator.triggerCustomEvent(
						serviceId, instanceId, monitoringId, ComotAction.MELA_STOP.toString(), null);

				UtilsTest.sleepSeconds(2);
				assertTrue(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
				assertFalse(isMonitored(instanceId));

				// manually start
				coordinator.triggerCustomEvent(
						serviceId, instanceId, monitoringId, ComotAction.MELA_START.toString(), null);

				UtilsTest.sleepSeconds(2);
				assertTrue(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
				assertTrue(isMonitored(instanceId));

				break;
			case ELASTIC_CHANGE:
				break;
			case UPDATE:
				break;

			case STOPPING:
				UtilsTest.sleepSeconds(2);
				assertTrue(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
				assertFalse(isMonitored(instanceId));
				break;
			case UNDEPLOYING:
				break;
			case FINAL:
				UtilsTest.sleepSeconds(2);
				assertFalse(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
				assertFalse(isMonitored(instanceId));
				break;

			case ERROR:
				fail("Should not reach ERROR state");
				break;

			default:
				break;
			}

		}

		coordinator.stopServiceInstance(serviceId, instanceId);

		log.info("{}", Utils.asJsonString(infoService.getService(serviceId)));

		UtilsTest.sleepSeconds(5);
		// UtilsTest.sleepInfinit();
	}

	protected boolean isMonitored(String instanceId) throws CoreServiceException {

		for (String id : monitoring.listAllServices()) {
			if (id.equals(instanceId)) {
				return true;
			}
		}
		return false;
	}

}
