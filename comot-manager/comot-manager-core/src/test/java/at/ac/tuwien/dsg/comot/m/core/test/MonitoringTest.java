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

import at.ac.tuwien.dsg.comot.m.common.ComotAction;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class MonitoringTest extends AbstractTest {

	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected Coordinator coordinator;
	@Autowired
	protected InformationServiceMock infoService;

	CloudService service;
	String serviceId;
	String instanceId;
	protected final String monitoringId = InformationServiceMock.MELA_SERVICE_PUBLIC_ID;
	protected final String deploymentId = InformationServiceMock.SALSA_SERVICE_PUBLIC_ID;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException {
		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

	}

	@Test(timeout = 240000)
	public void testMonitoring() throws IOException, JAXBException, ClassNotFoundException, EpsException {

		boolean isFresh = true;
		assertFalse(deployment.isManaged(instanceId));

		coordinator.assignSupportingOsu(serviceId, instanceId, deploymentId);
		coordinator.assignSupportingOsu(serviceId, instanceId, monitoringId);
		coordinator.startServiceInstance(serviceId, instanceId);

		while (true) {
			State state = lcManager.getCurrentState(instanceId, serviceId);

			if (state != null) {
				switch (state) {

				case INIT:
					break;
				case PASSIVE:
					if (isFresh) {

					} else {
						UtilsTest.sleepSeconds(3);
						assertTrue(infoService.isOsuAssignedToInstance(instanceId, monitoringId));
						assertFalse(isMonitored(instanceId));
						return;
					}
					break;
				case STARTING:
				case DEPLOYING:
					assertTrue(infoService.isOsuAssignedToInstance(instanceId, monitoringId));
					assertFalse(isMonitored(instanceId));
					break;

				case RUNNING:

					UtilsTest.sleepSeconds(3);
					assertTrue(infoService.isOsuAssignedToInstance(instanceId, monitoringId));
					assertTrue(isMonitored(instanceId));

					// manually stop
					coordinator.triggerCustomEvent(
							serviceId, instanceId, monitoringId, ComotAction.MELA_STOP.toString(), null);

					UtilsTest.sleepSeconds(3);
					assertTrue(infoService.isOsuAssignedToInstance(instanceId, monitoringId));
					assertFalse(isMonitored(instanceId));

					// manually start
					coordinator.triggerCustomEvent(
							serviceId, instanceId, monitoringId, ComotAction.MELA_START.toString(), null);

					UtilsTest.sleepSeconds(3);
					assertTrue(infoService.isOsuAssignedToInstance(instanceId, monitoringId));
					assertTrue(isMonitored(instanceId));

					coordinator.stopServiceInstance(serviceId, instanceId);
					isFresh = false;
					break;

				case STOPPING:
				case UNDEPLOYING:
					UtilsTest.sleepSeconds(3);
					assertTrue(infoService.isOsuAssignedToInstance(instanceId, monitoringId));
					assertFalse(isMonitored(instanceId));
					break;

				case FINAL:
					// UtilsTest.sleepSeconds(3);
					// assertFalse(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
					// assertFalse(isMonitored(instanceId));
					break;

				case ERROR:
					fail("Should not reach ERROR state");
					break;

				default:
					break;
				}
			}
			UtilsTest.sleepSeconds(5);
		}

	}

	protected boolean isMonitored(String instanceId) throws EpsException {

		for (String id : monitoring.listAllServices()) {
			if (id.equals(instanceId)) {
				return true;
			}
		}
		return false;
	}

}
