package at.ac.tuwien.dsg.comot.m.cs.test.clients;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
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

	public static final String SERVICE_ID = "HelloElasticityNoDB";

	@Test
	public void helperDeploy() throws EpsException, IOException {

		String xmlTosca = Utils
				.loadFileFromSystemAsString("./../resources/test/helloElasticity/HelloElasticityNoDB.xml");
		salsa.deploy(xmlTosca);
	}

	@Test
	public void testStartControl() throws EpsException, InterruptedException, JAXBException, IOException,
			ComotException {

		CloudService service = deployment.getService(SERVICE_ID);
		service = deployment.refreshStatus(service);

		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		DeploymentDescription deploymentDescription = deploymentMapper.extractDeployment(service);

		log.info("{}", UtilsCs.asString(cloudServiceXML));
		log.info("{}", UtilsCs.asString(deploymentDescription));

		controlClient.sendInitialConfig(service);

		controlClient.startControl(SERVICE_ID);
	}

	// @Test
	// public void startControl() throws EpsException, InterruptedException, JAXBException {
	// controlClient.startControl(SERVICE_ID);
	//
	// }

	@Test
	public void stopControl() throws EpsException, InterruptedException, JAXBException {
		controlClient.stopControl(SERVICE_ID);

	}

	@Test
	public void removeService() throws EpsException, InterruptedException, JAXBException {
		controlClient.removeService(SERVICE_ID);
	}

	@Test
	public void listService() throws EpsException, InterruptedException, JAXBException {
		List<String> list = controlClient.listAllServices();

		log.info("{}", list);
	}

}
