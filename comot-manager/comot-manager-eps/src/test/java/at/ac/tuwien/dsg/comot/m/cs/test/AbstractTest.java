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
package at.ac.tuwien.dsg.comot.m.cs.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.eps.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextEpsClients.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

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

}
