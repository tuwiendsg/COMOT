package at.ac.tuwien.dsg.comot.servrec.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.core.dal.ServiceRepoProxy;
import at.ac.tuwien.dsg.comot.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.repo.CloudServiceRepoWorkaround;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.recorder.model.Change;
import at.ac.tuwien.dsg.comot.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.servrec.RecordingManager;
import at.ac.tuwien.dsg.comot.servrec.spring.AppContextServrec;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppContextServrec.class)
@ActiveProfiles({ AppContextServrec.IMPERMANENT_NEO4J_DB, AppContextCore.EMBEDDED_H2_DB })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;
	@Resource
	protected Environment env;

	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected TestBean testBean;

	@Autowired
	protected CloudServiceRepoWorkaround cloudServiceRepo;
	@Autowired
	protected RecordingManager recordingManager;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	@Autowired
	protected ComotOrchestrator orchestrator;
	@Autowired
	protected ServiceRepoProxy serviceRepo;

	@Autowired
	protected ToscaMapper mapperTosca;

	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected ExecutionEngine engine;

	protected WrappingNeoServerBootstrapper srv;

	@Before
	public void setUp() {
		// http://neo4j.com/docs/1.8.3/server-embedded.html
		// http://127.0.0.1:7474/
		srv = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) db);
		srv.start();

		serviceRepo.setFake(true);

	}

	@After
	public void cleanUp() {
		srv.stop();
	}

	public CloudService update1(CloudService service) throws ClassNotFoundException, IOException {
		CloudService updatedService = (CloudService) Utils.deepCopy(service);// createService();

		// add parameter -> create new state & update relation to the old one
		updatedService.setName("UPDATED");

		ServiceTopology topo = updatedService.getServiceTopologiesList().get(0);
		ServiceUnit unit = UtilsTest.getServiceUnit(updatedService, STemplates.swId_unit);

		// remove relationship -> update timestamp
		topo.getServiceUnits().remove(unit);

		unit.setName("serviceNameUPDATED");

		// add node
		ServiceTopology newTopo = new ServiceTopology("newTopo_UPDATED");

		// add relationship s
		newTopo.addServiceUnit(unit);
		updatedService.addServiceTopology(newTopo);

		// change parameter of relationship -> create new one & update state of the old
		for (StackNode node : topo.getNodes()) {
			if (node.getId().equals(STemplates.swNodeId2) && node.getConnectToList().size() > 0) {
				node.getConnectToList().get(0).setVariableValue("variableValue_UPDATED");
			}
		}
		return updatedService;
	}

	public CloudService update2(CloudService service) throws ClassNotFoundException, IOException {

		CloudService finalService = (CloudService) Utils.deepCopy(service);

		// add parameter -> create new state & update relation to the old one
		finalService.setName("UPDATE_2");

		ServiceTopology topo1 = finalService.getServiceTopologiesList().get(0);
		ServiceTopology topo2 = finalService.getServiceTopologiesList().get(1);
		ServiceUnit unit2 = UtilsTest.getServiceUnit(finalService, STemplates.swId2_unit);

		// remove relationship -> update timestamp
		topo1.getServiceUnits().remove(unit2);
		finalService.getServiceTopologies().remove(topo1);

		// add relationship s
		topo2.addTopology(topo1);

		return finalService;
	}

	public void assertLabels(Long expected, Class<?> clazz) {
		assertLabels(expected, clazz.getSimpleName());
	}

	public void assertLabels(Long expected, String label) {
		Long count = testBean.countLabel(label);
		assertEquals(expected, count);
	}

	public int countChanges(Change change) {
		int i = 0;
		while (change != null) {
			i++;
			log.debug("stateFROM: {}", change.getFrom());
			log.debug("change: {}", change);
			log.debug("stateTO: {}\n", change.getTo());
			change = change.getTo().getEnd();
		}
		return i;
	}

}
