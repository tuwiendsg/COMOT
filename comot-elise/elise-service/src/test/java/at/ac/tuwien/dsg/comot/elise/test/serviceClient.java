/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.test;

import at.ac.tuwien.dsg.comot.elise.common.DataAccessInterface;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import sun.java2d.pipe.BufferedOpCodes;
//import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 *
 * @author hungld
 */
public class serviceClient {

    static String endpoint = "http://localhost:8080/elise-service/rest";

    public static void main(String[] args) {

        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JacksonJaxbJsonProvider()));
//        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JSONProvider()));
        
        System.out.println(da.health());

        System.out.println(da.cleanDB());
        
        Provider p = new Provider("MyProvider", Provider.ProviderType.IAAS);
        p.setId("uniqueID1");
        p.setName("Name 1");
        System.out.println("add provider"+da.addProvider(p));
        
        OfferedServiceUnit osu1 = new OfferedServiceUnit("OSU 1", p.getId());
        osu1.setId("OSU_ID_1");
        System.out.println("add offer: "+da.addOfferServiceUnit(osu1, p.getId()));
        
        // show which just saved 
        Set<Provider> providers = da.getProviders();
        for (Provider p1 : providers) {
            System.out.println("Provider GraphID: " + p1.getGraphID() + ",provider ID:"+ p1.getId() + ", type:"+p1.getProviderType() +", name:" + p1.getName());
            Set<OfferedServiceUnit> osus = da.getOfferedServiceOfProvider(p1.getId());
            for (OfferedServiceUnit o:osus){
                System.out.println("OSU ID: "+o.getId() + ", name: " + o.getName());
            }            
        }
        
        System.out.println("_____________N E X T");
        
        // add 2nd time the provider there
        Provider p2 = new Provider("MyProvider", Provider.ProviderType.IAAS);
        p2.setId("uniqueID1");
        p2.setName("Name 2");
        System.out.println("add provider"+da.addProvider(p2));
        System.out.println("SAVED PROVIDER 2ND TIME !");
        providers = da.getProviders();
        // if it is exist 
        for (Provider p1 : providers) {
            System.out.println("Provider GraphID: " + p1.getGraphID() + ",provider ID:"+ p1.getId() + ", type:"+p1.getProviderType() +", name:" + p1.getName());
            Set<OfferedServiceUnit> osus = da.getOfferedServiceOfProvider(p1.getId());
            for (OfferedServiceUnit o:osus){
                System.out.println(o.getId() + ", name: " + o.getName());
            }
        }
        // conclusion: the uniqueID will update node properties

    }
}
