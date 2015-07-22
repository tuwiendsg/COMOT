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

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class SalsaMappingTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(SalsaMappingTest.class);

	@Autowired
	protected ToscaMapper mapperTosca;

	@Test
	public void automatedMapperTest() throws JAXBException {

		CloudService serviceForMapping = STemplates.fullServiceWithoutInstances();

		LOG.info("original {}", Utils.asXmlString(serviceForMapping));

		Definitions tosca1 = mapperTosca.extractTosca(serviceForMapping);
		LOG.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service2 = mapperTosca.createModel(tosca1);
		LOG.info("service2 {}", Utils.asXmlString(service2));
		assertReflectionEquals(serviceForMapping, service2, ReflectionComparatorMode.LENIENT_ORDER);

		Definitions tosca2 = mapperTosca.extractTosca(service2);
		LOG.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service3 = mapperTosca.createModel(tosca2);
		LOG.info("service3 {}", Utils.asXmlString(service3));
		assertReflectionEquals(serviceForMapping, service3, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void testToscaFromFile() throws JAXBException, IOException {

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/daas_m2m_fromSalsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/tomcat_from_salsa.xml");
		LOG.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service1 = mapperTosca.createModel(tosca1);
		LOG.info("service1 {}", Utils.asXmlString(service1));

		Definitions tosca2 = mapperTosca.extractTosca(service1);
		LOG.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service2 = mapperTosca.createModel(tosca2);
		LOG.info("service2 {}", Utils.asXmlString(service2));
		assertReflectionEquals(service1, service2, ReflectionComparatorMode.LENIENT_ORDER);

	}

}
