/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.EliseDBService;
import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.OfferedServiceUnitDAO;
import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.ProviderDAO;
import at.ac.tuwien.dsg.comot.elise.settings.DataCollectorConfiguration;
import at.ac.tuwien.dsg.comot.elise.settings.PropertiesManager;
import at.ac.tuwien.dsg.comot.model.provider.Entity;
import at.ac.tuwien.dsg.comot.model.provider.MetricValue;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Provider;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import static org.neo4j.cypher.internal.compiler.v1_9.commands.expressions.StringHelper$class.props;

/**
 *
 * @author hungld
 */
public abstract class GenericCollector {
    // an unique ID for each collector
    UUID uuid;
    // readable name
    String name;
    // Logger which can set from outside
    Logger logger;
    // The entity that collector is collecting data for, can be a specific resource, quality or cost, or all the service unit.
    String entityID;
    
    String endpoint;
    
    EndpointType endpointType;
    
    public enum EndpointType{
        ELISE_REST, MQTT
    }
    
    {
        this.logger = Logger.getLogger(this.getClass());
    }
    
    public GenericCollector(String name, String entityID, String endpoint, EndpointType endpointType){
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.entityID = entityID;
        this.endpoint = endpoint;
        this.endpointType = endpointType;
    }
    
    /**
     * default with ELISE rest service 
     */
    public GenericCollector(String name, String entityID, String endpoint){
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.entityID = entityID;
        this.endpoint = endpoint;
        this.endpointType = EndpointType.ELISE_REST;
    }
    
    /**
     * Default with elise localhost rest endpoint
     */
    public GenericCollector(String name, String entityID){
        this.name = name;
        this.uuid = UUID.randomUUID();
        // if there is no entity ID provided, assume that there is new entity, which will be created in ELISE DB
        if (entityID==null){
            this.entityID = UUID.randomUUID().toString();
        } else {
            this.entityID = entityID;
        }
        this.endpoint = "http://localhost:8480/elise-service/rest";
        this.endpointType = EndpointType.ELISE_REST;
    }
    
    
    // read the elise.conf
    public GenericCollector(){
        this.uuid = UUID.randomUUID();
        this.name = PropertiesManager.getParameter(GenericCollectorParameters.name, DataCollectorConfiguration.ELISE_COLLECTOR_CONFIG_FILE);
        this.endpoint = PropertiesManager.getParameter(GenericCollectorParameters.endpoint, DataCollectorConfiguration.ELISE_COLLECTOR_CONFIG_FILE);
        this.entityID = PropertiesManager.getParameter(GenericCollectorParameters.entityUUID, DataCollectorConfiguration.ELISE_COLLECTOR_CONFIG_FILE);                
        logger.debug("Generic collector read from configure file (elise.conf): " + this.endpoint );
    }

    public Logger getLogger() {
        return logger;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }
        
    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
    
    public void sendData(){        
        Entity data = this.collect();
        logger.debug("Start to send data to the endpoint: " + this.endpoint + ", of type: " + this.endpointType);
        logger.debug("The data is updated for the entity: " + this.entityID +", class: " + data.getClass().getSimpleName());
        
                
        EliseDBService eliseDB =JAXRSClientFactory.create(this.endpoint, EliseDBService.class, Collections.singletonList(new JacksonJsonProvider()));        
        if (eliseDB == null){
            logger.debug("EliseDB is null");
            return;
        }
        logger.debug("Check ELISE service health (endpoint is ELISE): " + eliseDB.health());
        
//        if (this.entityID==null || this.entityID.isEmpty()){
//            String newUUID=data.getId();
//            writeAdaptorConfig(GenericCollectorParameters.entityUUID, newUUID);
//        }
        logger.debug("Sending an entity of type: " + data.getClass().getSimpleName());
        if (data.getClass().equals(Provider.class)){
            ProviderDAO providerDAO = JAXRSClientFactory.create(this.endpoint, ProviderDAO.class, Collections.singletonList(new JacksonJsonProvider()));        
            providerDAO.addProvider((Provider) data);
        } else if (data.getClass().equals(OfferedServiceUnit.class)){
            OfferedServiceUnitDAO osuDAO = JAXRSClientFactory.create(this.endpoint, OfferedServiceUnitDAO.class, Collections.singletonList(new JacksonJsonProvider()));        
            osuDAO.updateOfferServiceUnit((OfferedServiceUnit) data);           
        }
        
        System.out.println("Data send done !");
    }
    
    // just for test
    public void sendData(Entity data){        
        logger.debug("Start to send data to the endpoint: " + this.endpoint + ", of type: " + this.endpointType);
        logger.debug("The data is updated for the entity: " + this.entityID +", class: " + data.getClass().getSimpleName());
        
                
        EliseDBService eliseDB =JAXRSClientFactory.create(this.endpoint, EliseDBService.class, Collections.singletonList(new JacksonJsonProvider()));        
        logger.debug("Check ELISE service health (endpoint is ELISE): " + eliseDB.health());
        
//        if (this.entityID==null || this.entityID.isEmpty()){
//            String newUUID=data.getId();
//            writeAdaptorConfig(GenericCollectorParameters.entityUUID, newUUID);
//        }
        logger.debug("Sending an entity of type: " + data.getClass().getSimpleName());
        if (data.getClass().equals(Provider.class)){
            ProviderDAO providerDAO = JAXRSClientFactory.create(this.endpoint, ProviderDAO.class, Collections.singletonList(new JacksonJsonProvider()));        
            providerDAO.addProvider((Provider) data);
        } else if (data.getClass().equals(OfferedServiceUnit.class)){
            OfferedServiceUnitDAO osuDAO = JAXRSClientFactory.create(this.endpoint, OfferedServiceUnitDAO.class, Collections.singletonList(new JacksonJsonProvider()));        
            osuDAO.updateOfferServiceUnit((OfferedServiceUnit) data);           
        }
        
        System.out.println("Data send done !");
    }
    
    
    public String readAdaptorConfig(String key){
        Properties prop = new Properties();
        try {
            InputStream input = new FileInputStream(DataCollectorConfiguration.COLLECTOR_ADAPTOR_CONFIG_FILE);
            prop.load(input);
            return prop.getProperty(key);
        } catch (FileNotFoundException e) {
            logger.error("Do not found configuration file for adaptor. Error: " + e.getMessage());
        } catch (IOException e1) {
            logger.error("Cannot read configuratin file for adaptor. Error: " + e1.getMessage());
        }
        return null;
    }
    
    public String writeAdaptorConfig(String key, String value){
        Properties prop = new Properties();
        try {
            InputStream input = new FileInputStream(DataCollectorConfiguration.COLLECTOR_ADAPTOR_CONFIG_FILE);
            prop.load(input);
            input.close();
            
            FileOutputStream out = new FileOutputStream(DataCollectorConfiguration.COLLECTOR_ADAPTOR_CONFIG_FILE);
            prop.setProperty(key, value);
            // store and log
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");            
            prop.store(out, "Updated by collector " + this.getUuid() +" at: " + dateFormat.format(new Date()));
            out.close();            
            return prop.getProperty(key);
        } catch (FileNotFoundException e) {
            logger.error("Do not found configuration file for adaptor. Error: " + e.getMessage());
        } catch (IOException e1) {
            logger.error("Cannot read configuratin file for adaptor. Error: " + e1.getMessage());
        }
        return null;
    }
        
    public abstract Entity collect();

}
