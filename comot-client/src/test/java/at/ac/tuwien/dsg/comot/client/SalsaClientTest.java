package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import at.ac.tuwien.dsg.comot.samples.DataAsAServiceCloudApplication;
import org.junit.Test;

import static at.ac.tuwien.dsg.comot.client.SalsaClient.SalsaClientAction.DEPLOY;
import static at.ac.tuwien.dsg.comot.client.SalsaClient.SalsaClientAction.UNDEPLOY;
import static org.junit.Assert.assertEquals;

/**
 * @author omoser
 */

public class SalsaClientTest {

    @Test
    public void deployAndUndeployCloudApplication() throws Exception {
        SalsaClient client = buildSalsaClient();
        CloudApplication application = DataAsAServiceCloudApplication.build();
        SalsaResponse response = client.deploy(application);
        assertEquals(DEPLOY.expectedResultCode(), response.getCode());
        String serviceId = response.getMessage();
        response = client.undeploy(serviceId);
        assertEquals(UNDEPLOY.expectedResultCode(), response.getCode());
    }

    private SalsaClient buildSalsaClient() {
        SalsaClient client = new DefaultSalsaClient();
        client.getConfiguration().setBaseUri("/rest");
        return client;
    }

}
