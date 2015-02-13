package at.ac.tuwien.dsg.comot.m.cs.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextEps.class })
// @ActiveProfiles({ ApplicationContext.SPRING_PROFILE_TEST })
// @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
// @DatabaseSetup("classpath:iata_codes/airports_functional.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected Environment env;

	@Autowired
	protected SalsaClient salsaClient;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	protected String swNodeId = "nodeId";
	protected String serviceId = "serviceId";

	protected DeploymentDescription deploymentDescription;

	@Before
	public void startup() {

		// Deployment description

		AssociatedVM vm = new AssociatedVM();
		vm.setIp("10.99.0.85");
		vm.setUuid("93d785cc-f915-4127-81eb-0797b75de1a6");

		List<AssociatedVM> list = new ArrayList<>();
		list.add(vm);

		DeploymentUnit dUnit = new DeploymentUnit();
		dUnit.setServiceUnitID(swNodeId);
		dUnit.setAssociatedVMs(list);

		List<DeploymentUnit> deployments = new ArrayList<>();
		deployments.add(dUnit);

		deploymentDescription = new DeploymentDescription();
		deploymentDescription.setAccessIP("localhost");
		deploymentDescription.setCloudServiceID(serviceId);
		deploymentDescription.setDeployments(deployments);
	}

	public static Definitions loadTosca(String path)
			throws JAXBException, IOException {

		Definitions xmlContent = null;

		JAXBContext context = JAXBContext.newInstance(Definitions.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		xmlContent = (Definitions) unmarshaller
				.unmarshal(Utils.loadFileFromSystem(path));

		return xmlContent;
	}

}
