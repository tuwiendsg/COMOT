/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector;

import at.ac.tuwien.dsg.comot.elise.common.EliseManager.CollectorDescription;
import at.ac.tuwien.dsg.comot.elise.common.EliseManager.EliseManager;
import at.ac.tuwien.dsg.comot.elise.settings.DataCollectorConfiguration;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 *
 * @author hungld
 */
public abstract class GenericCollector {

    String collectorID;
    String name;

    public static enum EndpointType {

        ELISE_REST, MQTT;

        private EndpointType() {
        }
    }

    public static enum EntityType {

        OfferedServiceUnit, ServiceUnit, ServiceInstance;

        private EntityType() {
        }
    }

    Logger logger = Logger.getLogger(getClass());
    String serviceUnitUUID;
    EntityType entityType;
    String endpoint;
    EndpointType endpointType;
//    EliseManager eliseService;

    public GenericCollector(String collectorName) {
        this.name = DataCollectorConfiguration.getName();
        this.endpoint = DataCollectorConfiguration.getELISE_REST_ENDPOINT();
        this.collectorID = DataCollectorConfiguration.getELISE_collectorID();
        this.logger.debug("Generic collector read (elise.conf): endpoint = " + this.endpoint);
        this.logger.debug("Generic collector read (elise.conf): name = " + this.name);
        this.logger.debug("Generic collector read (elise.conf): collectorID = " + this.collectorID);
        registerCollector();

        this.logger.debug("Generic collector read from configure file (elise.conf): collectorID = " + this.serviceUnitUUID);
    }

    public String getName() {
        return this.name;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public EndpointType getEndpointType() {
        return this.endpointType;
    }

//    public EliseManager getEliseService() {
//        return this.eliseService;
//    }

    private void registerCollector() {
        logger.debug("Registering the collector: " + this.collectorID);
        logger.debug("REST endpoint: " + DataCollectorConfiguration.getELISE_REST_ENDPOINT_LOCAL());
        EliseManager eliseService = ((EliseManager) JAXRSClientFactory.create(DataCollectorConfiguration.getELISE_REST_ENDPOINT_LOCAL(), EliseManager.class, Collections.singletonList(new JacksonJsonProvider())));
        //EliseManager eliseService = ((EliseManager) JAXRSClientFactory.create("http://localhost:8483/elise-service/rest/", EliseManager.class, Collections.singletonList(new JacksonJsonProvider())));
        if (eliseService == null) {
            logger.error("Cannot call EliseManager service !!!! eliseService == null when connecting to: " + DataCollectorConfiguration.getELISE_REST_ENDPOINT_LOCAL());            
        } else {
            logger.debug("Created a client to elise service 111");
            
        }
        
        logger.debug("Local ELISE health: " + eliseService.health());
        CollectorDescription des = new CollectorDescription(this.collectorID, this.serviceUnitUUID, this.name);   
        
        eliseService.registerCollector(des);
    }

    public final String readAdaptorConfig(String key) {
        return readConfigProperty(key, DataCollectorConfiguration.COLLECTOR_ADAPTOR_CONFIG_FILE);
    }

    private String readConfigProperty(String key, String propertiesFile) {
        Properties prop = new Properties();
        try {
            InputStream input = new FileInputStream(propertiesFile);
            prop.load(input);
            return prop.getProperty(key);
        } catch (FileNotFoundException e) {
            this.logger.error("Do not found configuration file for adaptor. Error: " + e.getMessage());
        } catch (IOException e1) {
            this.logger.error("Cannot read configuratin file for adaptor. Error: " + e1.getMessage());
        }
        return null;
    }

    public String getCollectorID() {
        return this.collectorID;
    }

    public String getServiceUnitUUID() {
        return this.serviceUnitUUID;
    }

    public abstract void sendData();

    public abstract Object collect();

  
}
