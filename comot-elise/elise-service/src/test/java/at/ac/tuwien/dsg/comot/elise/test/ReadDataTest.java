/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.test;

import at.ac.tuwien.dsg.comot.elise.common.DataAccessInterface;
import static at.ac.tuwien.dsg.comot.elise.test.serviceClient.endpoint;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.MetricValue;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Provider;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Resource;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.ResourceOrQualityType;
import java.util.Collections;
import java.util.Set;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

/**
 *
 * @author hungld
 */
public class ReadDataTest {
     static String endpoint = "http://localhost:8080/elise-service/rest";

    public static void main(String[] args) {
        DataAccessInterface da = JAXRSClientFactory.create(endpoint, DataAccessInterface.class, Collections.singletonList(new JacksonJaxbJsonProvider()));
        
         System.out.println(da.health());

         System.out.println("ALL OSUS");
         Set<OfferedServiceUnit> osuss = da.getOfferServiceUnits();
         for(OfferedServiceUnit o:osuss){
             System.out.println("OSU ID: "+o.getId() + ", name: " + o.getName());
         }
         
         System.out.println("CHECK FOR EACH PROVIDER");
        // show which just saved 
        Set<Provider> providers = da.getProviders();
        for (Provider p1 : providers) {
            System.out.println("Provider GraphID: " + p1.getGraphID() + ",provider ID:"+ p1.getId() + ", type:"+p1.getProviderType() +", name:" + p1.getName());
            Set<OfferedServiceUnit> osus = da.getOfferedServiceOfProvider(p1.getId());
            for (OfferedServiceUnit o:osus){
                System.out.println("OSU ID: "+o.getId() + ", name: " + o.getName());
                OfferedServiceUnit vmOSU = da.getOfferServiceUnitByID(o.getId());
                System.out.println("vmosu queried: " + vmOSU.getId());
                Set<ResourceOrQualityType> rqts = da.getOfferServiceUnitResourceOrQualityTypeList(vmOSU.getId());
                for(ResourceOrQualityType rq:rqts){
                    System.out.println("Resource/Quality for: " + vmOSU.getId() + ":" + rq.getId());
                }
            }            
        }
        
        System.out.println("\n \n Get the VM OSU");
        OfferedServiceUnit vmOSU = da.getOfferServiceUnitByID("DSGOpenStack.openstack.VirtualInfrastructure");
        System.out.println("vmOSU, graphID: " + vmOSU.getGraphID() + ", Size of resources: " + vmOSU.getResources().size());
        
        // find resources of FlavorResource Type, of service unit VM
        System.out.println("\n\n Flavor resource of " + vmOSU.getId());
        Set<Resource> res = da.getOfferedServiceUnitResourceByType(vmOSU.getId(), "ResourceOrQualityType.FlavorResource");
        for (Resource r:res){
            System.out.println("Resource: " + r.getName());
            System.out.println("r.hasmetric.size: " + r.getHasMetric().size());
        }
        Set<MetricValue> metrics = da.getResourceMetricDetails("DSGOpenStack.openstack.VirtualInfrastructure.ECassandraNodeSnap_g");
        System.out.println("metricValue set side: " + metrics.size());
        
        Resource resX = da.getOfferedServiceUnitResourceByID("DSGOpenStack.openstack.VirtualInfrastructure","DSGOpenStack.openstack.VirtualInfrastructure.ECassandraNodeSnap_g");
        System.out.println("resource set set side: " + resX.getHasMetric().size());
        
        
        
    }
}
