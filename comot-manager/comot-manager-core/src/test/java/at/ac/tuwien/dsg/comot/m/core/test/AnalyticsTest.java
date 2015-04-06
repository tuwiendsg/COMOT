package at.ac.tuwien.dsg.comot.m.core.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.core.Recording;
import at.ac.tuwien.dsg.comot.m.core.analytics.AnalyticEngine;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class AnalyticsTest extends AbstractTest {

	// protected WrappingNeoServerBootstrapper srv;

	@Autowired
	private AnalyticEngine engine;
	@Autowired
	protected RevisionApi revisionApi;

	// @Before
	// public void setUp() {
	// // http://neo4j.com/docs/1.8.3/server-embedded.html
	// // http://127.0.0.1:7474/
	// srv = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) db);
	// srv.start();
	// }
	//
	// @After
	// public void cleanUp() {
	// srv.stop();
	// }

	@Test
	public void aaaaa() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, RecorderException {

		CloudService service = STemplates.fullService();
		cutResources(service);
		Navigator nav = new Navigator(service);
		String osInstId = STemplates.instanceId(STemplates.osNodeId);

		// change 0 created the instance

		Long time1 = System.currentTimeMillis();
		Map<String, Object> changeProperties = createProps(Action.DEPLOYMENT_STARTED.toString(), time1);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_LIFECYCLE, changeProperties);

		// change 1 still deploying

		nav.getInstance(osInstId).setState(State.DEPLOYING);
		nav.getInstance(osInstId).setEnvId("aaa");

		changeProperties = createProps("CONFIGURING", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_CUSTOM, changeProperties);

		// change 2 finished deployemnt

		nav.getInstance(osInstId).setState(State.RUNNING);
		Long time2 = System.currentTimeMillis();
		changeProperties = createProps(Action.DEPLOYED.toString(), time2);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_LIFECYCLE, changeProperties);

		// change 3

		nav.getInstance(osInstId).setEnvId("bbb");
		;
		changeProperties = createProps("SOME_EVENT", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_LIFECYCLE, changeProperties);

		// UtilsTest.sleepInfinit();

		engine.deploymentEvents(STemplates.serviceId, STemplates.serviceId);

	}

	public static Map<String, Object> createProps(String action, Long time) {
		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put(Recording.PROP_EVENT_NAME, action);
		changeProperties.put(Recording.PROP_EVENT_TIME, time);
		return changeProperties;
	}

	public static void cutResources(CloudService service) {
		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			OfferedServiceUnit osu = unit.getOsuInstance().getOsu();
			osu.setResources(null);
		}
	}

}
