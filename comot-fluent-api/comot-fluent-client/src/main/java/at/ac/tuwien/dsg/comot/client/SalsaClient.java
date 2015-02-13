package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.common.model.CloudService;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * @author omoser
 */
public interface SalsaClient {

    public enum SalsaClientAction {

        DEPLOY(SC_CREATED), UNDEPLOY(SC_OK), SPAWN(SC_OK), DESTROY(SC_OK), STATUS(SC_OK), SERVICE_DEPLOYMENT_INFO(SC_OK);

        final int expectedHttpResultCode;

        SalsaClientAction(int expectedHttpResultCode) {
            this.expectedHttpResultCode = expectedHttpResultCode;
        }

        public int expectedResultCode() {
            return expectedHttpResultCode;
        }
    }

    SalsaResponse deploy(CloudService CloudService) throws SalsaClientException;

    SalsaResponse undeploy(String serviceId) throws SalsaClientException;

    SalsaResponse spawn(String serviceId, String topologyId, String nodeId, int instanceCount) throws SalsaClientException;

    SalsaResponse destroy(String serviceId, String topologyId, String nodeId, String instanceId) throws SalsaClientException;

    SalsaResponse status(String serviceId) throws SalsaClientException;
    
    SalsaResponse getServiceDeploymentInfo(String serviceId);

    SalsaClientConfiguration getConfiguration();


}
