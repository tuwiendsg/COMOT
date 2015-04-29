package at.ac.tuwien.dsg.comot.m.core.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.analytics.ElasticityAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.analytics.ResultLine;
import at.ac.tuwien.dsg.comot.m.core.analytics.TimeAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;

public class EvalTest extends AbstractTest {

	protected String templateId = "HelloElasticity";

	protected TestAgentAdapter agent;
	protected String staticDeplId;

	@Autowired
	private TimeAnalyzis engine;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected ElasticityAnalyzis elAnalysis;
	@Autowired
	protected TestBean testBean;

	int number = 5;

	int delay = 90; // seconds

	@Test
	public void oneSALSA() throws Exception {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		List<String> services = new ArrayList<>();

		for (int i = 0; i < number; i++) {
			services.add(coordinator.createService(infoService.getTemplate(templateId).getDescription()));

			agent.assertLifeCycleEvent(Action.CREATED);

			coordinator.assignSupportingOsu(services.get(i), staticDeplId);

			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		}

		for (int i = 0; i < number; i++) {
			UtilsTest.sleepSeconds(delay);
			coordinator.startService(services.get(i));
		}

		for (int i = 0; i < number; i++) {
			agent.waitForLifeCycleEvent(Action.DEPLOYED);
			agent.waitForLifeCycleEvent(Action.DEPLOYED);
			agent.waitForLifeCycleEvent(Action.DEPLOYED);
			agent.waitForLifeCycleEvent(Action.DEPLOYED);
		}

		log.info("FINISHED");

		results(services);

	}

	@Test
	public void multipleDynamicSALSA() throws Exception {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		// deploy dynamic salsas
		for (int i = 0; i < number; i++) {
			UtilsTest.sleepSeconds(delay);

			OfferedServiceUnit salsaOsu = infoService.getOsu(Constants.SALSA_SERVICE_DYNAMIC);
			coordinator.createDynamicService(salsaOsu.getId());
		}

		for (int i = 0; i < number; i++) {
			agent.waitForCustomEvent(EpsEvent.EPS_DYNAMIC_CREATED.toString());
		}

		log.info("ALL SALSAS CREATED");

		List<String> services = new ArrayList<>();
		List<OsuInstance> salsas = infoService.getEpsInstances(InformationClient.DYNAMIC);

		UtilsTest.sleepSeconds(300);

		// create services
		for (int i = 0; i < number; i++) {

			services.add(coordinator.createService(infoService.getTemplate(templateId).getDescription()));

			agent.assertLifeCycleEvent(Action.CREATED);

			coordinator.assignSupportingOsu(services.get(i), salsas.get(i).getId());

			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		}

		// deploy services
		for (int i = 0; i < number; i++) {
			UtilsTest.sleepSeconds(delay);
			coordinator.startService(services.get(i));
		}

		UtilsTest.sleepSeconds(2000);

		// for (int i = 0; i < number; i++) {
		// agent.waitForLifeCycleEvent(Action.DEPLOYED);
		// agent.waitForLifeCycleEvent(Action.DEPLOYED);
		// agent.waitForLifeCycleEvent(Action.DEPLOYED);
		// agent.waitForLifeCycleEvent(Action.DEPLOYED);
		// }

		log.info("FINISHED");

		results(services);

	}

	public void results(List<String> services) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, RecorderException {

		Double stagingSum = 0.0;
		int stagingNr = 0;
		Double totalSum = 0.0;
		int totalNr = 0;

		Double eventTimeSum = 0.0;
		int eventTimeNr = 0;

		for (int i = 0; i < number; i++) {
			for (ResultLine line : engine.deploymentEvents(services.get(i))) {

				if (line.getStage().equals("STAGING")) {
					stagingSum = stagingSum + line.getLength();
					stagingNr++;
				}

				if (line.getStage().equals("SUM")) {
					totalSum = totalSum + line.getLength();
					totalNr++;
				}
				log.info(line.toString());
			}

			eventTimeSum = eventTimeSum + testBean.computeDuration(services.get(i));
			eventTimeNr++;
		}

		log.info("STAGING: {}", stagingSum / stagingNr);
		log.info("TOTAL: {}", totalSum / totalNr);
		log.info("AVG eventTime: {} ", eventTimeSum / eventTimeNr);
	}
}
