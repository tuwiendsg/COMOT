/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector.staticinfo;

import at.ac.tuwien.dsg.comot.elise.collector.ProviderCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Provider;
import java.io.File;
import java.io.FileInputStream;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This module reads the Provider description in .txt files in current folder.
 * @author hungld
 */
public class StaticInfoCollector extends ProviderCollector {

//    public static final String providerFile = "provider.txt";

    public StaticInfoCollector(String collectorName) {
        super("StaticInfoCollector");
    }

    
    @Override
    public Provider collect() {
        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();
        File providerTxt = null;
        if (listOfFiles.length>0){
            providerTxt = listOfFiles[0];  // just take the first file. May
        } else {
            System.out.println("Error: There is no .txt file found to read. Please put the provider description in a .txt file and place in the same folder!");
            return null;
        }
        
        try {
            try (FileInputStream inputStream = new FileInputStream(providerTxt)) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(inputStream, Provider.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
