package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class RecordingTest extends AbstractTest {

	CloudService service;
	String serviceId;
	String instanceId;
	String monitoringId;
	String deploymentId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException {
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/tosca/tomcat/tomcat_from_salsa.xml");
		service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);

		monitoringId = InformationServiceMock.MELA_SERVICE_PUBLIC_ID;
		deploymentId = InformationServiceMock.SALSA_SERVICE_PUBLIC_ID;
	}

	@Test
	public void testSimple() throws IOException, JAXBException, ClassNotFoundException, EpsException {

		State state;
		Change change;

		// CREATE
		instanceId = coordinator.createServiceInstance(serviceId);

		// while (lcManager.getCurrentState(instanceId, serviceId) != State.RUNNING) {
		// UtilsTest.sleepSeconds(2);
		// state = lcManager.getCurrentState(instanceId, serviceId);
		//
		// switch (state) {
		//
		// case INIT:
		// case PASSIVE:
		// UtilsTest.sleepSeconds(2);
		//
		// change = revisionApi.getAllChangesThatModifiedThisObject(instanceId, serviceId, 0L, Long.MAX_VALUE);
		// assertEquals(1, countChanges(change));
		// change = revisionApi.getAllChanges(instanceId, 0L, Long.MAX_VALUE);
		// assertEquals(1, countChanges(change));
		//
		// // ASSIGN
		// coordinator.assignSupportingOsu(serviceId, instanceId, monitoringId);
		// coordinator.startServiceInstance(serviceId, instanceId);
		//
		// case STARTING:
		// UtilsTest.sleepSeconds(2);
		//
		// change = revisionApi.getAllChangesThatModifiedThisObject(instanceId, serviceId, 0L, Long.MAX_VALUE);
		// assertEquals(1, countChanges(change));
		// change = revisionApi.getAllChanges(instanceId, 0L, Long.MAX_VALUE);
		// assertEquals(3, countChanges(change));
		//
		// return;
		//
		// case DEPLOYING:
		// case RUNNING:
		// case ELASTIC_CHANGE:
		// case UPDATE:
		//
		// case STOPPING:
		// case UNDEPLOYING:
		// case FINAL:
		// break;
		//
		// case ERROR:
		// fail("Should not reach ERROR state");
		// break;
		// default:
		// break;
		// }
		// }

	}

	public int countChanges(Change change) throws JAXBException {
		int i = 0;
		while (change != null) {
			i++;
			log.info(Utils.asJsonString(change));
			change = change.getTo().getEnd();
		}
		return i;
	}

}
