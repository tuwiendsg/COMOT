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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;

//http://stackoverflow.com/questions/3428273/validate-jaxbelement-in-jpa-jax-rs-web-service/3440388#3440388
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class ToscaValidatingReader implements MessageBodyReader<Definitions> {

	protected static final Logger LOG = LoggerFactory.getLogger(ToscaValidatingReader.class);

	private static final String TOSCA_URI = "http://docs.oasis-open.org/tosca/TOSCA/v1.0/os/schemas/TOSCA-v1.0.xsd";

	@Context
	protected Providers providers;
	private Schema schema;

	public ToscaValidatingReader() {
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schema = sf.newSchema(new URL(TOSCA_URI));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return arg0 == Definitions.class;
	}

	public Definitions readFrom(Class<Definitions> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5)
			throws IOException, WebApplicationException {
		try {
			JAXBContext jaxbContext = null;
			ContextResolver<JAXBContext> resolver = providers.getContextResolver(JAXBContext.class, arg3);
			if (null != resolver) {
				jaxbContext = resolver.getContext(arg0);
			}
			if (null == jaxbContext) {
				jaxbContext = JAXBContext.newInstance(arg0);
			}
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);

			LOG.debug("TOSCA validation passed");

			return (Definitions) unmarshaller.unmarshal(arg5);
		} catch (UnmarshalException e) {
			LOG.info("{}", e);
			throw new ComotIllegalArgumentException("Validation of TOSCA failed: " + e.getCause());
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

}