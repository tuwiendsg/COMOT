package at.ac.tuwien.dsg.comot.m.core.test;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.m.core.dal.ServiceRepoProxy;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextCore.class })
@ActiveProfiles({ AppContextCore.EMBEDDED_H2_DB}) //AppContextCore.INSERT_INIT_DATA 
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected Environment env;

	@Autowired
	protected ServiceRepoProxy serviceRepo;

	@Autowired
	protected ComotOrchestrator orchestrator;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	@Autowired
	protected ToscaMapper mapperTosca;
}
