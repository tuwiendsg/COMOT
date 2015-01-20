package at.ac.tuwien.dsg.comot.servrec.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class RecorderTest extends AbstractTest {

	protected CloudService service;

	@Before
	public void startup() {
		service = STemplates.fullService();
	}

	@Test
	public void testRecordingManager() throws IOException, JAXBException, CoreServiceException, ComotException,
			IllegalArgumentException, IllegalAccessException {

		CloudService service = mapperTosca.createModel(UtilsCs
				.loadTosca(UtilsTest.TEST_FILE_BASE + "tomcat/tomcat.xml"));

		// deploy
		orchestrator.deployNew(service);

		ServiceEntity entity = serviceRepo.findOne(service.getId());
		log.info("entity: {}", entity);
		log.info("recordingManager: {}", recordingManager);

		recordingManager.addService(service.getId(), deployment, control, monitoring);

		recordingManager.insertVersion(entity.getServiceOriginal());
		recordingManager.insertVersion(entity.getServiceDeployed());

		recordingManager.startRecording(service.getId());

		UtilsTest.sleepInfinit();

	}

	@Test
	public void testAtTomcat() throws JAXBException, IOException, IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, RecorderException {

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "tomcat/tomcat.xml");
		CloudService service = mapperTosca.createModel(def);

		log.info("{}", Utils.asString(service));

		revisionApi.createOrUpdateRegion(service, service.getId(), "init");

		Object oResult = revisionApi.getRevision(service.getId(), "deployWar",
				Long.MAX_VALUE);
		log.info("oResult {}", oResult);
		log.info("oResult XML {}", Utils.asXmlString(oResult));

		UtilsTest.sleepInfinit();
	}

}
