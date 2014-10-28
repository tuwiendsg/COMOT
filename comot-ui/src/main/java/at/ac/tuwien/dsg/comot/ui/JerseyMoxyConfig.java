package at.ac.tuwien.dsg.comot.ui;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import at.ac.tuwien.dsg.comot.ui.service.SampleResource;


/**
 * https://jersey.java.net/documentation/latest/media.html#json.moxy
 * @author Juraj
 *
 */
public class JerseyMoxyConfig extends ResourceConfig {
 
       
    public JerseyMoxyConfig(){
    	
        register(RequestContextFilter.class);
        register(SampleResource.class);     
       // addProperties(properties);    
    }
}
