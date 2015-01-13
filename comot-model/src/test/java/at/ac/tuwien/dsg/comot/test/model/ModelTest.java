package at.ac.tuwien.dsg.comot.test.model;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.model.AppContextModel;
import at.ac.tuwien.dsg.comot.model.repo.CloudServiceRepo;
import at.ac.tuwien.dsg.comot.model.repo.CloudServiceRepoWorkaround;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.ServiceTemplates;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextModelTest.class, AppContextModel.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ModelTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;
	@Resource
	protected Environment env;

	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected ExecutionEngine engine;
	protected WrappingNeoServerBootstrapper srv;

	@Autowired
	protected CloudServiceRepo csRepo;

	@Autowired
	CloudServiceRepoWorkaround testBeanBean;

	@Before
	public void setUp() {
		// http://neo4j.com/docs/1.8.3/server-embedded.html
		// http://127.0.0.1:7474/
		srv = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) db);
		srv.start();

		engine = new ExecutionEngine(db);
	}

	@After
	public void cleanUp() {
		srv.stop();
	}

	/**
	 * Execute and check the result in the console http://127.0.0.1:7474/
	 */
	@Test
	public void testModel() throws InterruptedException {
		CloudService service = ServiceTemplates.fullServiceWithoutInstances();
		// log.info("");

		testBeanBean.save(service);

		while (true) {
			Thread.sleep(1000);
		}
	}

}
