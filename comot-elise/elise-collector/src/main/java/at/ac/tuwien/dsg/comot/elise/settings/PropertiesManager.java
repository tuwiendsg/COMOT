/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.settings;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author hungld
 */
public class PropertiesManager {
    protected static Logger logger = Logger.getLogger("EliseLogger");
    
    static public Properties getParameters(String configFile){
        Properties configuration = new Properties();        
        try {	
                File f = new File(configFile);
                if (!f.exists()){
                        logger.error("Configuration file not found: " + configFile);				
                        return null;
                } else {
                        configuration.load(new FileReader(f));                      
                }
        } catch (Exception ex) {
                ex.printStackTrace();
                return null;
        }
        return configuration;
    }
    
    static public String getParameter(String key, String configFile){        
        Properties configuration = getParameters(configFile);
        if (configuration != null){
            logger.debug("Trying to get parameter: "+key+" in the configuration file:" + configFile + ", value: " + configuration.getProperty(key));
            return configuration.getProperty(key);
        } else {
            logger.debug("Cannot load the configuration file:" + configFile);
        }
        return null;
    }
    
    public Logger getLogger(){
        return logger;
    }
}