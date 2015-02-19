/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector;

import at.ac.tuwien.dsg.comot.elise.common.DataAccessInterface;
import at.ac.tuwien.dsg.comot.elise.common.DataProviderInterface;
import at.ac.tuwien.dsg.comot.elise.settings.DataCollectorConfiguration;
import at.ac.tuwien.dsg.comot.elise.settings.PropertiesManager;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Provider;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 *
 * @author hungld
 * This class send data to REST interface of Ecosystem server or Merging server
 */
public class DataSender {
    String restURL;
    // which class is using for collector. The class must extends the DataProvider
    DataProviderInterface provider;
    String endpoint = "";
    
    public DataSender(String endpoint, String classname){
        // TODO: endpoint is the elise-server API, should elaborate for Message queue also
        this.endpoint = endpoint;
        String dataProviderClass = classname;
        
        try {
            System.out.println("Trying to instantiate class: " + dataProviderClass);
            provider = (DataProviderInterface) Class.forName(dataProviderClass).newInstance();
            System.out.println("Class initiation done !");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.out.println("An error when creating object of: " + dataProviderClass);
            Logger.getLogger(DataSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String readAndSendData(){
        Provider providerData = (Provider)provider.readData();
        System.out.println("Connecting to send data: \"" + endpoint + "\"");
        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JacksonJsonProvider()));
//        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JacksonJaxbJsonProvider()));

        System.out.println(da.health());
        
        System.out.println(da.addProvider(providerData));        
        return "Read and send data completely !";
    }
    
    
    
    
    // REST manager
    
    public static enum HttpVerb {
	        GET, POST, PUT, DELETE, OTHER;

	        public static HttpVerb fromString(String method) {
	            try {
	                return HttpVerb.valueOf(method.toUpperCase());
	            } catch (Exception e) {
	                return OTHER;
	            }
	        }
	    }
    
    
    
    
}
