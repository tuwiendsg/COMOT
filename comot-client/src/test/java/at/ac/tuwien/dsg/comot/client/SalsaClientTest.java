package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.cloud.salsa.common.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.comot.ComotContext;
import at.ac.tuwien.dsg.comot.NoopSalsaEngineApi;
import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import at.ac.tuwien.dsg.comot.samples.DataAsAServiceCloudApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author omoser
 */

@ContextConfiguration(classes = ComotContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SalsaClientTest extends AbstractSalsaClientTest {

    @Test
    public void deployAndUndeployCloudApplication() throws Exception {
        SalsaClient client = prepareSalsaClient();
        CloudApplication application = DataAsAServiceCloudApplication.build();
        SalsaResponse response = client.deploy(application);
        assertTrue(response.isExpected());
        assertEquals(SalsaClient.SalsaClientAction.DEPLOY.expectedResultCode(), response.getCode());
        String serviceId = response.getMessage();

        response = client.undeploy(serviceId);
        assertEquals(SalsaClient.SalsaClientAction.UNDEPLOY.expectedResultCode(), response.getCode());
    }

    @Test
    public void undeployNonExistingService() {
        SalsaClient client = prepareSalsaClient();
        SalsaResponse response = client.undeploy("does+not+exist");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getCode());
    }

    @Test
    public void spawnAndDestroyAdditionalInstance() throws IOException {
        SalsaClient client = prepareSalsaClient();
        SalsaResponse response = client.spawn("service", "topoplogy", "node", 1);
        assertEquals(Response.Status.OK.getStatusCode(), response.getCode());
        ObjectMapper mapper = new ObjectMapper();
        String[] instanceIds = mapper.reader(String[].class).readValue(response.getMessage());
        assertEquals(1, instanceIds.length);

        response = client.spawn("service", "topoplogy", "node", 3);
        assertEquals(Response.Status.OK.getStatusCode(), response.getCode());
        instanceIds = mapper.reader(String[].class).readValue(response.getMessage());
        assertEquals(3, instanceIds.length);

        for (String instanceId : instanceIds) {
            response = client.destroy("service", "topology", "node", instanceId);
            assertEquals(Response.Status.OK.getStatusCode(), response.getCode());
        }
    }

    @Test
    public void destroyNonExistingInstance() {
        SalsaClient client = prepareSalsaClient();
        SalsaResponse response = client.destroy("service", "topology", "node", "does_not_exist");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getCode());

    }

    @Test
    public void checkServiceStatus() {
        SalsaClient client = prepareSalsaClient();
        //client.getConfiguration().withHost("128.130.172.215").withPort(8080);
        SalsaResponse response = client.status("cfd5c6e6-de0c-4484-9b73-0ae7562c8f86");
        assertTrue(response instanceof SalsaServiceStatusResponse);
        SalsaServiceStatusResponse statusResponse = ((SalsaServiceStatusResponse) response);
        assertEquals("DaaS Pilot", statusResponse.getCloudEntityId());
        assertEquals(SalsaEntityState.RUNNING, statusResponse.getCloudEntityState());
        assertFalse(statusResponse.getChildren().isEmpty());

        Optional<SalsaServiceStatusResponse> osHeadNodeOption = statusResponse.getChildren().stream().filter(child -> "OS_HeadNode".equals(child.getCloudEntityId())).findFirst();
        assertTrue(osHeadNodeOption.isPresent());
        Optional<SalsaServiceStatusResponse> osHeadNode0Option = osHeadNodeOption.get().getChildren().stream().filter(child -> "OS_HeadNode_0".equals(child.getCloudEntityId())).findFirst();
        assertTrue(osHeadNode0Option.isPresent());
        SalsaServiceStatusResponse osHeadNode0 = osHeadNode0Option.get();
        assertEquals("OS_HeadNode_0", osHeadNode0.getCloudEntityId());
        assertNotNull(osHeadNode0.getMonitoringData());
        assertFalse(osHeadNode0.getMonitoringData().getMetrics().isEmpty());

        MonitoringData monitoringData =  osHeadNode0.getMonitoringData();
        assertEquals("os-headnode-0.novalocal", monitoringData.getName());
        assertEquals("10.99.0.18", monitoringData.getIp());

        osHeadNode0.getMonitoringData().getMetrics().stream().allMatch(metric -> metric.getName() != null);
        osHeadNode0.getMonitoringData().getMetrics().stream().allMatch(metric -> metric.getType() != null);
        osHeadNode0.getMonitoringData().getMetrics().stream().allMatch(metric -> metric.getValue() != null);
    }

    private SalsaClient prepareSalsaClient() {
        SalsaClient client = new DefaultSalsaClient();
        client.getConfiguration()
                .withPort(AbstractSalsaClientTest.port)
                .withBaseUri("/")
                .withValidatingToscaBuilder(false);

        return client;
    }


    @Override
    protected Class getServiceBeanClass() {
        return NoopSalsaEngineApi.class;
    }

    @Override
    protected String getServiceBeanName() {
        return "noopSalsaEngineApi";
    }
}
