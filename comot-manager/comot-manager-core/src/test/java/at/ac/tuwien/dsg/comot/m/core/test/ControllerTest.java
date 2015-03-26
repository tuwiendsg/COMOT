package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;

public class ControllerTest extends AbstractTest {

	protected String serviceId;

	protected final String MONITORING_ID = Constants.MELA_SERVICE_PUBLIC_ID;
	protected final String DEPLOYMENT_ID = Constants.SALSA_SERVICE_PUBLIC_ID;
	protected final String CONTROL_ID = Constants.RSYBL_SERVICE_PUBLIC_ID;

	protected final String INSTANCE_ID = "HelloElasticityNoDB";// "HelloElasticityNoDB";

	@Test
	public void onlyMonitoring() throws ClassNotFoundException, IOException, JAXBException, EpsException {

		log.info("----------------------------------------------------------");

		serviceId = infoService.getServiceInstance(INSTANCE_ID).getId();

		assertTrue(deployment.isManaged(INSTANCE_ID));

		coordinator.assignSupportingOsu(serviceId, INSTANCE_ID, MONITORING_ID);
		UtilsTest.sleepSeconds(5);

	}

	@Test
	public void testControl() throws IOException, JAXBException, ClassNotFoundException, EpsException {

		assertTrue(deployment.isManaged(INSTANCE_ID));

		log.info("----------------------------------------------------------");

		serviceId = infoService.getServiceInstance(INSTANCE_ID).getId();

		assertTrue(deployment.isManaged(INSTANCE_ID));

		coordinator.assignSupportingOsu(serviceId, INSTANCE_ID, CONTROL_ID);
		UtilsTest.sleepSeconds(3);

		assertTrue(control.isControlled(INSTANCE_ID));

		UtilsTest.sleepInfinit();

		// while (true) {
		// State state = lcManager.getCurrentState(INSTANCE_ID, serviceId);
		//
		// if (state != null) {
		// switch (state) {
		//
		// case INIT:
		// break;
		// case PASSIVE:
		// if (isFresh) {
		//
		// } else {
		// UtilsTest.sleepSeconds(3);
		// assertTrue(infoService.isOsuAssignedToInstance(INSTANCE_ID, MONITORING_ID));
		// assertFalse(isMonitored(INSTANCE_ID));
		// return;
		// }
		// break;
		// case STARTING:
		// case DEPLOYING:
		// assertTrue(infoService.isOsuAssignedToInstance(INSTANCE_ID, MONITORING_ID));
		// assertFalse(isMonitored(INSTANCE_ID));
		// break;
		//
		// case RUNNING:
		//
		// UtilsTest.sleepSeconds(3);
		// assertTrue(infoService.isOsuAssignedToInstance(INSTANCE_ID, MONITORING_ID));
		// assertTrue(isMonitored(INSTANCE_ID));
		//
		// // manually stop
		// coordinator.triggerCustomEvent(
		// serviceId, INSTANCE_ID, MONITORING_ID, ComotAction.MELA_STOP.toString(), null);
		//
		// UtilsTest.sleepSeconds(3);
		// assertTrue(infoService.isOsuAssignedToInstance(INSTANCE_ID, MONITORING_ID));
		// assertFalse(isMonitored(INSTANCE_ID));
		//
		// // manually start
		// coordinator.triggerCustomEvent(
		// serviceId, INSTANCE_ID, MONITORING_ID, ComotAction.MELA_START.toString(), null);
		//
		// UtilsTest.sleepSeconds(3);
		// assertTrue(infoService.isOsuAssignedToInstance(INSTANCE_ID, MONITORING_ID));
		// assertTrue(isMonitored(INSTANCE_ID));
		//
		// coordinator.stopServiceInstance(serviceId, INSTANCE_ID);
		// isFresh = false;
		// break;
		//
		// case STOPPING:
		// case UNDEPLOYING:
		// UtilsTest.sleepSeconds(3);
		// assertTrue(infoService.isOsuAssignedToInstance(INSTANCE_ID, MONITORING_ID));
		// assertFalse(isMonitored(INSTANCE_ID));
		// break;
		//
		// case FINAL:
		// // UtilsTest.sleepSeconds(3);
		// // assertFalse(infoService.isOsuAssignedToInstance(serviceId, instanceId, monitoringId));
		// // assertFalse(isMonitored(instanceId));
		// break;
		//
		// case ERROR:
		// fail("Should not reach ERROR state");
		// break;
		//
		// default:
		// break;
		// }
		// }
		// UtilsTest.sleepSeconds(5);
		// }

	}

}
