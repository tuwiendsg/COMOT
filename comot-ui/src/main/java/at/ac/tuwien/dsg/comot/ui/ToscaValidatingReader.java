package at.ac.tuwien.dsg.comot.ui;

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

import at.ac.tuwien.dsg.comot.common.exception.ComotIllegalArgumentException;

//http://stackoverflow.com/questions/3428273/validate-jaxbelement-in-jpa-jax-rs-web-service/3440388#3440388
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class ToscaValidatingReader implements MessageBodyReader<Definitions> {

	protected static final Logger log = LoggerFactory.getLogger(ToscaValidatingReader.class);

	private static final String TOSCA_URI = "http://docs.oasis-open.org/tosca/TOSCA/v1.0/os/schemas/TOSCA-v1.0.xsd"; // TODO
																														// load
																														// from
																														// localhost

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

			log.debug("TOSCA validation passed");

			return (Definitions) unmarshaller.unmarshal(arg5);
		} catch (UnmarshalException e) {
			throw new ComotIllegalArgumentException("Validation of TOSCA failed: " + e.getCause());
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

}