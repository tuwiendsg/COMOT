/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.cs.test.clients;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
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

public class RsyblClientManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(RsyblClientManualTest.class);

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

	public static final String SERVICE_ID = "HelloElasticity_1";

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

		LOG.info("{}", UtilsCs.asString(cloudServiceXML));
		LOG.info("{}", UtilsCs.asString(deploymentDescription));

		// controlClient.setHostAndPort("128.130.172.191", 8280);
		controlClient.setHostAndPort("localhost", 8280);

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

		LOG.info("{}", list);
	}

}
