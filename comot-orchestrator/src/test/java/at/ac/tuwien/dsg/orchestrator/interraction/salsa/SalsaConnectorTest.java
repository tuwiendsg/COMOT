/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.orchestrator.interraction.salsa;

import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import org.junit.*;

/**
 *
 * @author daniel-tuwien
 */
public class SalsaConnectorTest {

    public SalsaConnectorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getServiceDeploymentInfo method, of class SalsaInterraction.
     */
    @Test
    public void testGetServiceDeploymentInfo() {
        System.out.println("getServiceDeploymentInfo");
        String serviceId = "DaasService";
        SalsaConnector instance = new SalsaConnector();

        DeploymentDescription result = instance.getServiceDeploymentInfo(serviceId);

//        StringWriter out = new StringWriter();
//        try {
//            JAXBContext a = JAXBContext.newInstance(DeploymentDescription.class);
//            Marshaller u = a.createMarshaller();
//            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//            u.marshal(result, out);
//
//            String content = out.toString();
//            System.out.println(content);
//
//        } catch (JAXBException e) {
//            e.printStackTrace();
//        }

    }
    
//    
//    
//    private a(String serviceId, String topologyId, String nodeId, int instanceCount){
//      
//        URI spawnUri = UriBuilder.fromPath(configuration.getSpawnPath()).build(serviceId, topologyId, nodeId, instanceCount);
//        HttpPost method = new HttpPost(spawnUri);
//       HttpHost endpoint = new HttpHost("localhost", 8080);
//        try {
//            return handleResponse(httpClient.execute(endpoint, method), salsaAction);
//            
//        return executeMethod(method, SalsaClient.SalsaClientAction.SPAWN);
//    }
//    }

}
