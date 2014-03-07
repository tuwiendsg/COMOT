package at.ac.tuwien.dsg.comot.model;

/**
 * Created by omoser on 3/1/14.
 */
public class Constraint extends AbstractCloudEntity {

    private Metric metric;

    private Operator operator;

    private String value;

    public Constraint(String id) {
        super(id);
    }

    public static Constraint Constraint(String id) {
        return new Constraint(id);
    }

    public Constraint value(final String value) {
        this.value = value;
        return this;
    }

    public Constraint should(final Operator operator) {
        this.operator = operator;
        return this;
    }

    public Constraint forMetric(final Metric metric) {
        this.metric = metric;
        return this;
    }

    public Metric getMetric() {
        return metric;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public enum Metric {

        Latency("latency", "ms"),
        CpuUsage("cpuUsage", "%"),
        ResponseTime("responseTime", "ms"),
        Cost("cost", "$"),
        Throughput("throughgput", "");

        private final String metric;

        private final String unit;

        Metric(String metric, String unit) {
            this.metric = metric;
            this.unit = unit;
        }

        public final String getMetric() {
            return metric;
        }

        public final String getUnit() {
            return unit;
        }
    }

    public enum Operator {

        LessThan("&lt;"),
        GreaterThan("&gt;"),
        Equals("eq"),
        And("AND"),
        Or("OR"),
        UNDEF("__UNDEF__");

        private final String value;

        Operator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constraint)) return false;

        Constraint that = (Constraint) o;

        if (metric != that.metric) return false;
        if (operator != that.operator) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metric != null ? metric.hashCode() : 0;
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "metric=" + metric +
                ", operator=" + operator +
                ", value='" + value + '\'' +
                "} " + super.toString();
    }
}
