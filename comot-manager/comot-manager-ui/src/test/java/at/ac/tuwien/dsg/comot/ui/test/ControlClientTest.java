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
package at.ac.tuwien.dsg.comot.ui.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;

public class ControlClientTest extends AbstractTest {

	@Test
	public void testTest() throws EpsException, ComotException {

		// deployment.deploy(ExampleDeployOneVM.build());
	}

	@Test
	public void testJaxb() throws EpsException, JAXBException, IOException {

		// CompositionRulesConfiguration mcr = UtilsCs.loadMetricCompositionRules("aaaa",
		// UtilsTest.TEST_FILE_BASE + "mela/defCompositionRules.xml");
		//
		// UnifiedConfiguration unified = new UnifiedConfiguration();
		// unified.setEffects("effects");
		// unified.setMcr(mcr);
		//
		// try {
		// JAXBContext jaxbContext = JAXBContext.newInstance(UnifiedConfiguration.class);
		//
		// // marshall
		// StringWriter sw = new StringWriter();
		//
		// Marshaller marshaller = jaxbContext.createMarshaller();
		// marshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
		// marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, true);
		// marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//
		// marshaller.marshal(unified, sw);
		//
		// String xml = sw.toString();
		// log.info("marshalled:" + xml);
		//
		// // unmarshall
		// StringReader reader = new StringReader(xml);
		//
		// Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
		// unMarshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
		// unMarshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, true);
		//
		// UnifiedConfiguration customer = (UnifiedConfiguration) unMarshaller.unmarshal(reader);
		//
		// log.info("" + customer);
		//
		// sw = new StringWriter();
		// marshaller.marshal(customer.getMcr(), sw);
		// log.info(sw.toString());
		//
		// } catch (JAXBException e) {
		// e.printStackTrace();
		// }

	}

}
