/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.test;

import at.ac.tuwien.dsg.comot.elise.common.DataAccessInterface;
import java.util.Collections;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

/**
 *
 * @author hungld
 */
public class CleanDB {
       static String endpoint = "http://localhost:8080/elise-service/rest";

    public static void main(String[] args) {

        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JacksonJaxbJsonProvider()));
//        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JSONProvider()));
        
        System.out.println(da.health());

        System.out.println(da.cleanDB());
    }
}
