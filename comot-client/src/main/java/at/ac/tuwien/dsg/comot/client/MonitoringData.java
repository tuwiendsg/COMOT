package at.ac.tuwien.dsg.comot.client;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author omoser
 */

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MonitoringData {

    @JsonProperty
    private String name;

    @JsonProperty
    private String ip;

    @JsonProperty
    private String location;

    @JsonProperty
    private String tags;

    @JsonProperty
    private String reported;

    @JsonProperty
    private String tn;

    @JsonProperty
    private String dmax;

    @JsonProperty
    private String tmax;

    @JsonProperty
    private String gmondStarted;

    @JsonProperty
    private List<Metric> metrics = new ArrayList<>();

    public MonitoringData withName(final String name) {
        this.name = name;
        return this;
    }

    public MonitoringData withIp(final String ip) {
        this.ip = ip;
        return this;
    }

    public MonitoringData withLocation(final String location) {
        this.location = location;
        return this;
    }

    public MonitoringData withTags(final String tags) {
        this.tags = tags;
        return this;
    }

    public MonitoringData withReported(final String reported) {
        this.reported = reported;
        return this;
    }

    public MonitoringData withTn(final String tn) {
        this.tn = tn;
        return this;
    }

    public MonitoringData withDmax(final String dmax) {
        this.dmax = dmax;
        return this;
    }

    public MonitoringData withTmax(final String tmax) {
        this.tmax = tmax;
        return this;
    }

    public MonitoringData withGmondStarted(final String gmondStarted) {
        this.gmondStarted = gmondStarted;
        return this;
    }

    public MonitoringData withMetrics(final List<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getReported() {
        return reported;
    }

    public void setReported(String reported) {
        this.reported = reported;
    }

    public String getTn() {
        return tn;
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    public String getDmax() {
        return dmax;
    }

    public void setDmax(String dmax) {
        this.dmax = dmax;
    }

    public String getTmax() {
        return tmax;
    }

    public void setTmax(String tmax) {
        this.tmax = tmax;
    }

    public String getGmondStarted() {
        return gmondStarted;
    }

    public void setGmondStarted(String gmondStarted) {
        this.gmondStarted = gmondStarted;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    @JsonIgnoreProperties({"slope", "gangliaExtraDataInfoCollection"})
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Metric {

        @JsonProperty
        private String name;

        @JsonProperty
        private String type;

        @JsonProperty
        private String units;

        @JsonProperty
        private String value;

        @JsonProperty
        private String tn;

        @JsonProperty
        private String dmax;

        @JsonProperty
        private String tmax;

        public Metric withName(final String name) {
            this.name = name;
            return this;
        }

        public Metric withType(final String type) {
            this.type = type;
            return this;
        }

        public Metric withUnits(final String units) {
            this.units = units;
            return this;
        }

        public Metric withValue(final String value) {
            this.value = value;
            return this;
        }

        public Metric withTn(final String tn) {
            this.tn = tn;
            return this;
        }

        public Metric withDmax(final String dmax) {
            this.dmax = dmax;
            return this;
        }

        public Metric withTmax(final String tmax) {
            this.tmax = tmax;
            return this;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getTn() {
            return tn;
        }

        public void setTn(String tn) {
            this.tn = tn;
        }

        public String getDmax() {
            return dmax;
        }

        public void setDmax(String dmax) {
            this.dmax = dmax;
        }

        public String getTmax() {
            return tmax;
        }

        public void setTmax(String tmax) {
            this.tmax = tmax;
        }
    }


}
