package at.ac.tuwien.dsg.comot.m.info;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

/**
 * https://jersey.java.net/documentation/latest/media.html#json.moxy
 * 
 * @author Juraj
 *
 */
public class JerseyMoxyConfig extends ResourceConfig {

	public JerseyMoxyConfig() {
		// REST RESOURCES
		register(Resource.class);
		// CONFIGURATION
		register(RequestContextFilter.class);
	}
}
