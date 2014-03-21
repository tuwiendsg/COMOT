package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import at.ac.tuwien.dsg.comot.samples.DataAsAServiceCloudApplication;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author omoser
 */

public class SalsaClientTest {

    @Test
    public void deployCloudApplicationAndCheckResponse() throws Exception {
        SalsaClient client = new DefaultSalsaClient();
        client.getConfiguration().setBaseUri("/rest");
        CloudApplication application = DataAsAServiceCloudApplication.build();
        SalsaResponse response = client.deploy(application);
        assertEquals(SalsaClient.SalsaClientAction.DEPLOY.expectedResultCode(), response.getCode());
    }


}
