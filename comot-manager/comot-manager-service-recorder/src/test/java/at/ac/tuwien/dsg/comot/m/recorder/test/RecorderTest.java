package at.ac.tuwien.dsg.comot.m.recorder.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class RecorderTest extends AbstractTest {

	protected CloudService service;

	@Before
	public void startup() {
		service = STemplates.fullService();
	}

	@Test
	public void aaaaa() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, RecorderException {

		CloudService service = STemplates.fullService();
		cutOsus(service);
		Navigator nav = new Navigator(service);
		String osInstId = STemplates.instanceId(STemplates.osNodeId);

		// change 0 created the instance

		Long time1 = System.currentTimeMillis();
		Map<String, Object> changeProperties = createProps(Action.DEPLOYMENT_STARTED.toString(), time1);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_LIFECYCLE",
				changeProperties);

		// change 1 still deploying

		nav.getInstance(osInstId).setState(State.DEPLOYING);
		nav.getInstance(osInstId).setEnvId("aaa");

		changeProperties = createProps("CONFIGURING", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_CUSTOM", changeProperties);

		// change 2 finished deployemnt

		nav.getInstance(osInstId).setState(State.RUNNING);
		Long time2 = System.currentTimeMillis();
		changeProperties = createProps(Action.DEPLOYED.toString(), time2);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_LIFECYCLE",
				changeProperties);

		// change 3

		nav.getInstance(osInstId).setEnvId("bbb");
		;
		changeProperties = createProps("SOME_EVENT", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_LIFECYCLE",
				changeProperties);

		UtilsTest.sleepInfinit();

		// engine.aaa(STemplates.serviceId);

	}

	public static Map<String, Object> createProps(String action, Long time) {
		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put("Recording.PROP_EVENT_NAME", action);
		changeProperties.put("Recording.PROP_EVENT_TIME", time);
		return changeProperties;
	}

	@Test
	public void testRecordingManager() throws IOException, JAXBException, EpsException, ComotException,
			IllegalArgumentException, IllegalAccessException {

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "tosca/tomcat.xml");
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

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "tosca/tomcat.xml");
		CloudService service = mapperTosca.createModel(def);

		log.info("{}", Utils.asXmlString(service));

		revisionApi.createOrUpdateRegion(service, service.getId(), service.getId(), "init", null);

		Object oResult = revisionApi.getRevision(service.getId(), "deployWar",
				Long.MAX_VALUE);
		log.info("oResult {}", oResult);
		log.info("oResult XML {}", Utils.asXmlString(oResult));

		UtilsTest.sleepInfinit();
	}

}
