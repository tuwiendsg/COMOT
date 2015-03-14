package at.ac.tuwien.dsg.comot.m.cs.test.clients;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class RsyblClientTest extends AbstractTest {

	public static final String SERVICE_ID = "HelloElasticity_VM";
	// public static final String TOPOLOGY_ID = "example_topology";
	// public static final String NODE_ID = "example_OS_comot";
	//
	// private static final String NODE_IP = "10.99.0.26"; // <== check this

	@Autowired
	private SalsaClient salsa;
	@Autowired
	protected RsyblClient rsybl;
	@Autowired
	private ControlClient controlClient;

	@Autowired
	protected RsyblMapper rsyblMapper;
	@Autowired
	protected DeploymentMapper deploymentMapper;

	@Before
	public void setup() {

	}

	@Test
	public void helperDeploy() throws CoreServiceException, IOException {

		String xmlTosca = Utils.loadFile("./../resources/test/helloElasticity/HelloElasticity_VM.xml");
		salsa.deploy(xmlTosca);
	}

	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException, JAXBException, IOException,
			ComotException {

		CloudService service = deployment.getService(SERVICE_ID);
		service = deployment.refreshStatus(service);

		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		DeploymentDescription deploymentDescription = deploymentMapper.extractDeployment(service);

		log.info("{}", UtilsCs.asString(cloudServiceXML));
		log.info("{}", UtilsCs.asString(deploymentDescription));

		controlClient.sendInitialConfig(service);

	}

	@Test
	public void removeService() throws CoreServiceException, InterruptedException, JAXBException {
		controlClient.stopControl(SERVICE_ID);
	}

}
