package at.ac.tuwien.dsg.comot.ui;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.cs.UtilsCs;

// http://stackoverflow.com/questions/9086930/flexible-marshalling-with-jaxb/9089542#9089542
@Provider
@Produces({ "application/xml", "application/json" })
public class DefinitionsContextResolver implements ContextResolver<JAXBContext> {

	protected static final Logger log = LoggerFactory.getLogger(DefinitionsContextResolver.class);
	
	
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
			log.debug("Custom JAXB for org.oasis.tosca.Definitions JAX-RS works");
			return jc;
		}
		return null;
	}

}