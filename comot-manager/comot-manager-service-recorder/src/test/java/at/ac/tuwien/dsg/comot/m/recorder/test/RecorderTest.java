package at.ac.tuwien.dsg.comot.m.recorder.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class RecorderTest extends AbstractTest {

	protected CloudService service;

	@Before
	public void startup() {
		service = STemplates.fullService();
	}

	@Test
	public void testRecordingManager() throws IOException, JAXBException, EpsException, ComotException,
			IllegalArgumentException, IllegalAccessException {

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "tomcat/tomcat.xml");
		// CloudService service = mapperTosca.createModel(def);

		// // deploy
		// orchestrator.deployNew(UtilsCs.asString(def));
		//
		// ServiceEntity entity = serviceRepo.findOne(service.getId());
		// log.info("entity: {}", entity);
		// log.info("recordingManager: {}", recordingManager);
		//
		// recordingManager.addService(service.getId(), deployment, control, monitoring);
		//
		// recordingManager.insertVersion(entity.getServiceDeployed());
		//
		// recordingManager.startRecording(service.getId());
		//
		// UtilsTest.sleepInfinit();

	}

	@Test
	public void testAtTomcat() throws JAXBException, IOException, IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, RecorderException {

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "tomcat/tomcat.xml");
		CloudService service = mapperTosca.createModel(def);

		log.info("{}", Utils.asXmlString(service));

		revisionApi.createOrUpdateRegion(service, service.getId(), "init", null);

		Object oResult = revisionApi.getRevision(service.getId(), "deployWar",
				Long.MAX_VALUE);
		log.info("oResult {}", oResult);
		log.info("oResult XML {}", Utils.asXmlString(oResult));

		UtilsTest.sleepInfinit();
	}

}
