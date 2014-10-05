///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package at.ac.tuwien.dsg.orchestrator.interraction.salsa;
//
//import at.ac.tuwien.dsg.comot.client.SalsaClient;
//import at.ac.tuwien.dsg.comot.client.SalsaClientException;
//import at.ac.tuwien.dsg.comot.client.SalsaResponse;
//import at.ac.tuwien.dsg.comot.common.logging.Markers;
//import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
//import java.io.StringWriter;
//import java.net.URI;
//import javax.ws.rs.core.UriBuilder;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//import org.apache.http.HttpHost;
//import org.apache.http.client.methods.HttpPost;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// *
// * @author daniel-tuwien
// */
//public class SalsaInterractionTest {
//
//    public SalsaInterractionTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    /**
//     * Test of getServiceDeploymentInfo method, of class SalsaInterraction.
//     */
//    @Test
//    public void testGetServiceDeploymentInfo() {
//        System.out.println("getServiceDeploymentInfo");
//        String serviceId = "IoTDaaS";
//        SalsaInterraction instance = new SalsaInterraction();
//
//        DeploymentDescription result = instance.getServiceDeploymentInfo(serviceId);
//
////        StringWriter out = new StringWriter();
////        try {
////            JAXBContext a = JAXBContext.newInstance(DeploymentDescription.class);
////            Marshaller u = a.createMarshaller();
////            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
////
////            u.marshal(result, out);
////
////            String content = out.toString();
////            System.out.println(content);
////
////        } catch (JAXBException e) {
////            e.printStackTrace();
////        }
//    }
//
////    
////    
////    private a(String serviceId, String topologyId, String nodeId, int instanceCount){
////      
////        URI spawnUri = UriBuilder.fromPath(configuration.getSpawnPath()).build(serviceId, topologyId, nodeId, instanceCount);
////        HttpPost method = new HttpPost(spawnUri);
////       HttpHost endpoint = new HttpHost("128.130.172.215", 8080);
////        try {
////            return handleResponse(httpClient.execute(endpoint, method), salsaAction);
////            
////        return executeMethod(method, SalsaClient.SalsaClientAction.SPAWN);
////    }
////    }
//}
