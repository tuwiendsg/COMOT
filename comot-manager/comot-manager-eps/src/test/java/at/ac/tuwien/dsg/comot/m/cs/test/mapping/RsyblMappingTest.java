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

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class RsyblMappingTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(RsyblMappingTest.class);

	@Autowired
	protected RsyblOrika orika;
	@Autowired
	protected RsyblMapper mapper;

	protected CloudService serviceForMapping;

	@Before
	public void startup() {
		serviceForMapping = STemplates.fullServiceWithoutInstances();
	}

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException {

		LOG.info("original {}", Utils.asXmlString(serviceForMapping));

		CloudServiceXML rsybl = mapper.extractRsybl(serviceForMapping);
		LOG.info("rsybl {}", UtilsCs.asString(rsybl));

	}

	@Test
	public void orikaTest() throws JAXBException {

		LOG.info("original {}", Utils.asXmlString(serviceForMapping));

		CloudServiceXML xml = orika.get().map(serviceForMapping, CloudServiceXML.class);
		LOG.info("tosca1 {}", UtilsCs.asString(xml));

	}

}
