package at.ac.tuwien.dsg.comot.client;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;

/**
 * @author omoser
 */

@JsonIgnoreProperties({"isAbstract", "connectto"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SalsaServiceStatusResponse extends SalsaResponse {

    @JsonProperty("id")
    private String cloudEntityId;

    @JsonProperty("state")
    private SalsaEntityState cloudEntityState;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private List<SalsaServiceStatusResponse> children;

    @JsonProperty
    private String nodeType;

    @JsonProperty("monitoring")
    @JsonDeserialize(as = MonitoringData.class)
    private MonitoringData monitoringData;

    public SalsaServiceStatusResponse() {
    }

    public SalsaServiceStatusResponse(SalsaResponse response) {
        super(response.code, response.message);
    }

    public SalsaServiceStatusResponse withCloudEntityId(final String cloudEntityId) {
        this.cloudEntityId = cloudEntityId;
        return this;
    }

    public SalsaServiceStatusResponse withCloudEntityState(final SalsaEntityState cloudEntityState) {
        this.cloudEntityState = cloudEntityState;
        return this;
    }

    public SalsaServiceStatusResponse withProperties(final Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public SalsaServiceStatusResponse withChildren(final List<SalsaServiceStatusResponse> children) {
        this.children = children;
        return this;
    }

    public SalsaServiceStatusResponse withCode(final int code) {
        this.code = code;
        return this;
    }

    public SalsaServiceStatusResponse withMessage(final String message) {
        this.message = message;
        return this;
    }

    public SalsaServiceStatusResponse withExpectedCode(final int expectedCode) {
        this.expectedCode = expectedCode;
        return this;
    }

    public MonitoringData getMonitoringData() {
        return monitoringData;
    }

    public void setMonitoringData(MonitoringData monitoringData) {
        this.monitoringData = monitoringData;
    }

    public String getCloudEntityId() {
        return cloudEntityId;
    }

    public SalsaEntityState getCloudEntityState() {
        return cloudEntityState;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<SalsaServiceStatusResponse> getChildren() {
        return children;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }


    public void setCloudEntityId(String cloudEntityId) {
        this.cloudEntityId = cloudEntityId;
    }

    public void setCloudEntityState(SalsaEntityState cloudEntityState) {
        this.cloudEntityState = cloudEntityState;
    }

    public void setChildren(List<SalsaServiceStatusResponse> children) {
        this.children = children;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
