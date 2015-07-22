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
package at.ac.tuwien.dsg.comot.m.cs.test.mapping;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaMappingManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(SalsaMappingManualTest.class);

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;

	// test with https://github.com/tuwiendsg/SALSA/blob/master/examples/4-DeployWithTomcat.xml
	protected static final String TEST_SERVICE_ID = "comot_tomcat_id";

	@Test
	public void stateMapperTest() throws EpsException, JAXBException, ComotException {

		// ENRICH WITH STATE

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		LOG.info("tosca {}", UtilsCs.asString(def));
		LOG.info("state {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		LOG.info("service {}", Utils.asXmlString(service));

		mapperDepl.enrichModel(TEST_SERVICE_ID, service, serviceState);
		LOG.info("service enriched{}", Utils.asXmlString(service));

		// EXTRACT DEPLOYMENT DESCRIOPTION

		DeploymentDescription descr = mapperDepl.extractDeployment(service);
		LOG.info("depl {}", UtilsCs.asString(descr));

	}

}
