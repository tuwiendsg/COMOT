package at.ac.tuwien.dsg.comot.client;

import org.junit.Test;

/**
 * @author omoser
 */

public class SalsaClientTest {

    @Test
    public void deployCloudApplicationAndCheckResponse() {
        SalsaClient client = new DefaultSalsaClient();
        client.getConfiguration().setUri("/rest");


    }
}
