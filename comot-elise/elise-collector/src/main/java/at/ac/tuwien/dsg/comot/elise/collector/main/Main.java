/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector.main;

import at.ac.tuwien.dsg.comot.elise.collector.DataSender;
import at.ac.tuwien.dsg.comot.elise.settings.PropertiesManager;

/**
 *
 * @author hungld
 * 
 * Usage:
 * This file will read in the ./elise.conf for the configuration
 * java -cp *.jar at.ac.tuwien.dsg.comot.elise.collector.Main [provider_class_name] [propertyFile]
 * 
 * the propertyFile can be used for specific provider, e.g. openstack.conf, flexiant.conf
 * 
 */
public class Main {
    
    
    
    public static void main(String[] args){
        
        String provider_class_config=args[0];
        String properties_file=args[1];
        
        final String CURRENT_DIR = System.getProperty("user.dir");
        final String DATA_PROVIDER_CONFIG_FILE = CURRENT_DIR+"/elise.conf";
        
        String providerClassName = PropertiesManager.getParameter(provider_class_config, DATA_PROVIDER_CONFIG_FILE);       
        String endpoint = PropertiesManager.getParameter("sending_endpoint", DATA_PROVIDER_CONFIG_FILE);
        
        System.out.println("Start the data provider and data sender ... !");
        DataSender ds = new DataSender(endpoint, providerClassName);
        ds.readAndSendData();
    }
}
