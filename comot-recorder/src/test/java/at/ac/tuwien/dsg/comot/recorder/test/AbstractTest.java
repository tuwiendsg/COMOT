package at.ac.tuwien.dsg.comot.recorder.test;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.recorder.AppContextRecorder;
import at.ac.tuwien.dsg.comot.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.recorder.repo.RevisionRepo;
import at.ac.tuwien.dsg.comot.recorder.revisions.RevisionApi;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppContextRecorder.class)
@ActiveProfiles({ AppContextRecorder.SPRING_PROFILE_TEST })
// @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
// @DatabaseSetup("classpath:iata_codes/airports_functional.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected Environment env;

	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected RevisionRepo revisionRepo;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected TestBean testBean;

	@Autowired
	protected GraphDatabaseService db;

	protected ExecutionEngine engine;
	protected WrappingNeoServerBootstrapper srv;

	@Before
	public void setUp() {
		// http://neo4j.com/docs/1.8.3/server-embedded.html
		// http://127.0.0.1:7474/
		srv = new WrappingNeoServerBootstrapper((ImpermanentGraphDatabase) db);
		srv.start();

		engine = new ExecutionEngine(db);
	}

	@After
	public void cleanUp() {
		srv.stop();
	}

}
