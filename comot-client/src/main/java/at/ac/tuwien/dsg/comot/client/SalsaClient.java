package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.common.model.CloudApplication;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * @author omoser
 */
public interface SalsaClient {

    public enum SalsaClientAction {

        DEPLOY(SC_CREATED), UNDEPLOY(SC_OK), SPAWN(SC_OK), DESTROY(SC_OK);

        final int expectedHttpResultCode;

        SalsaClientAction(int expectedHttpResultCode) {
            this.expectedHttpResultCode = expectedHttpResultCode;
        }

        public int expectedResultCode() {
            return expectedHttpResultCode;
        }
    }

    SalsaResponse deploy(CloudApplication cloudApplication) throws Exception;

    SalsaResponse undeploy(String serviceId) throws Exception;

    SalsaResponse spawn(String serviceId, String topologyId, String nodeId, int instanceCount) throws Exception;

    SalsaResponse destroy(String servId, String topologyId, String nodeId, String instanceId) throws Exception;

    SalsaClientConfiguration getConfiguration();


}
