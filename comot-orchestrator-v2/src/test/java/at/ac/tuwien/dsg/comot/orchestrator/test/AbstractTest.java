package at.ac.tuwien.dsg.comot.orchestrator.test;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.client.test.TestClientContext;
import at.ac.tuwien.dsg.comot.core.ComotContext;
import at.ac.tuwien.dsg.comot.core.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.orchestrator.ComotOrchestrator;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { TestClientContext.class, TestOrchestratorContext.class, ComotContext.class })
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	protected Environment env;
	
	@Autowired
	protected ComotOrchestrator orchestrator;
	
	@Autowired
	protected ToscaDescriptionBuilder toscaBuilder;
	

}
