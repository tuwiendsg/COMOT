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
package at.ac.tuwien.dsg.comot.m.ui;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;

// properties of type object and additional classes for JAXB
// http://stackoverflow.com/questions/9086930/flexible-marshalling-with-jaxb/9089542#9089542
@Provider
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class DefinitionsContextResolver implements ContextResolver<JAXBContext> {

	private static final Logger LOG = LoggerFactory.getLogger(DefinitionsContextResolver.class);

	private JAXBContext jc;

	public DefinitionsContextResolver() {
		try {
			jc = JAXBContext.newInstance(UtilsCs.CONTEXT_TOSCA);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JAXBContext getContext(Class<?> clazz) {
		if (Definitions.class == clazz) {
			LOG.debug("Custom JAXB for org.oasis.tosca.Definitions JAX-RS works");
			return jc;
		}
		return null;
	}

}