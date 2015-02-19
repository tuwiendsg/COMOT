package at.ac.tuwien.dsg.comot.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author omoser
 */
@XmlRootElement(name = "BundleConfig")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"id", "deploymentConfig", "runtimeConfig"})
public class BundleConfig {

    private String id;

    @JsonProperty("deployment-config")
    private DeploymentConfig deploymentConfig;

    @JsonProperty("runtime-config")
    private RuntimeConfig runtimeConfig;

    public BundleConfig() {
    }

    @XmlElement(name = "Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "DeploymentConfig")
    public DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    public void setDeploymentConfig(DeploymentConfig deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }


    @XmlElement(name = "RuntimeConfig")
    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    public void setRuntimeConfig(RuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BundleConfig)) return false;

        BundleConfig that = (BundleConfig) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class DeploymentConfig {

        private String uri;

        private String version;

        public DeploymentConfig() {
        }

        @XmlElement(name = "Uri")
        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        @XmlElement(name = "Version")
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public DeploymentConfig withUri(final String uri) {
            this.uri = uri;
            return this;
        }

        public DeploymentConfig withVersion(final String version) {
            this.version = version;
            return this;
        }

    }

    @XmlType(name = "RuntimeConfig")
    public static class RuntimeConfig {

        private Map<String, String> environment = new HashMap<>();

        @JsonProperty("logging-config")
        private LoggingConfig loggingConfig = new LoggingConfig();

        private String arguments;

        public RuntimeConfig() {
        }


        @XmlElement(name = "Environment", type = HashMap.class)
        public Map<String, String> getEnvironment() {
            return environment;
        }

        public void addEnvironmentVariable(String var, String val) {
            environment.put(var, val);
        }

        public void setEnvironment(Map<String, String> environment) {
            this.environment = environment;
        }

        @XmlElement(name = "Arguments")
        public String getArguments() {
            return arguments;
        }

        public void setArguments(String arguments) {
            this.arguments = arguments;
        }

        public RuntimeConfig withEnvironment(final Map<String, String> environment) {
            this.environment = environment;
            return this;
        }

        public RuntimeConfig withArguments(final String arguments) {
            this.arguments = arguments;
            return this;
        }



        @XmlElement(name = "LoggingConfig")
        public LoggingConfig getLoggingConfig() {
            return loggingConfig;
        }

        public void setLoggingConfig(LoggingConfig loggingConfig) {
            this.loggingConfig = loggingConfig;
        }
    }

    public static class LoggingConfig {

        private String dir;

        public LoggingConfig() {
        }

        @XmlElement(name = "Directory")
        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public LoggingConfig withDir(final String dir) {
            this.dir = dir;
            return this;
        }
    }
}
