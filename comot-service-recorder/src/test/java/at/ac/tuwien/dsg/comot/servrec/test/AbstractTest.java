package at.ac.tuwien.dsg.comot.servrec.test;

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

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.core.dal.ServiceRepoProxy;
import at.ac.tuwien.dsg.comot.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.repo.CloudServiceRepoWorkaround;
import at.ac.tuwien.dsg.comot.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.servrec.RecordingManager;
import at.ac.tuwien.dsg.comot.servrec.spring.AppContextServrec;

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

}
