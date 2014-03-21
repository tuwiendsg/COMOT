package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import org.apache.http.HttpStatus;

/**
 * @author omoser
 */
public interface SalsaClient {

    public enum SalsaClientAction {

        DEPLOY(HttpStatus.SC_CREATED), UNDEPLOY(HttpStatus.SC_OK);

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

    SalsaClientConfiguration getConfiguration();


}
