package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.analytics.ElasticityAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.analytics.ResultLine;
import at.ac.tuwien.dsg.comot.m.core.analytics.TimeAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class EvalTest extends AbstractTest {

	protected String serviceId;
	protected String instanceId;

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
	public void oneSALSA() throws ClassNotFoundException, IOException, JAXBException,
			ShutdownSignalException, ConsumerCancelledException, InterruptedException, EpsException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, RecorderException {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		serviceId = "HelloElasticity";

		List<String> services = new ArrayList<>();

		for (int i = 0; i < number; i++) {
			services.add(coordinator.createServiceInstance(serviceId));

			agent.assertLifeCycleEvent(Action.CREATED);

			coordinator.assignSupportingOsu(serviceId, services.get(i), staticDeplId);

			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		}

		for (int i = 0; i < number; i++) {
			UtilsTest.sleepSeconds(delay);
			coordinator.startServiceInstance(serviceId, services.get(i));
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
	public void multipleDynamicSALSA() throws ClassNotFoundException, IOException, JAXBException,
			ShutdownSignalException, ConsumerCancelledException, InterruptedException, EpsException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, RecorderException {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		serviceId = "HelloElasticity";

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
		List<OsuInstance> salsas = infoService.getElasticPlatformServicesInstances(InformationClient.DYNAMIC);

		UtilsTest.sleepSeconds(300);
		
		// create services
		for (int i = 0; i < number; i++) {

			services.add(coordinator.createServiceInstance(serviceId));

			agent.assertLifeCycleEvent(Action.CREATED);

			coordinator.assignSupportingOsu(serviceId, services.get(i), salsas.get(i).getId());

			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
			agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		}

		// deploy services
		for (int i = 0; i < number; i++) {
			UtilsTest.sleepSeconds(delay);
			coordinator.startServiceInstance(serviceId, services.get(i));
		}

		UtilsTest.sleepSeconds(2000);
		
//		for (int i = 0; i < number; i++) {
//			agent.waitForLifeCycleEvent(Action.DEPLOYED);
//			agent.waitForLifeCycleEvent(Action.DEPLOYED);
//			agent.waitForLifeCycleEvent(Action.DEPLOYED);
//			agent.waitForLifeCycleEvent(Action.DEPLOYED);
//		}

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
			for (ResultLine line : engine.deploymentEvents(serviceId, services.get(i))) {

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
