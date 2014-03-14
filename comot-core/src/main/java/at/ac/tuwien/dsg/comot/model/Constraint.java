package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class Constraint extends AbstractCloudEntity implements Renderable {

    private Metric metric;

    private Operator operator;

    private String value;

    Constraint(String id) {
        super(id);
        context.put(id, this);
    }

    public static Constraint Constraint(String id) {
        return new Constraint(id);
    }

    public static Constraint LatencyConstraint(String id) {
        return new Constraint(id).ofType(ConstraintType.SYBL).forMetric(Metric.Latency);
    }

    public static Constraint ResponseTimeConstraint(String id) {
        return new Constraint(id).ofType(ConstraintType.SYBL).forMetric(Metric.ResponseTime);
    }

    public static Constraint ThroughputConstraint(String id) {
        return new Constraint(id).ofType(ConstraintType.SYBL).forMetric(Metric.Throughput);
    }

    public static Constraint CostConstraint(String id) {
        return new Constraint(id).ofType(ConstraintType.SYBL).forMetric(Metric.Cost);
    }

    public static Constraint CpuUsageConstraint(String id) {
        return new Constraint(id).ofType(ConstraintType.SYBL).forMetric(Metric.CpuUsage);
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

    @Override
    public Constraint withId(String id) {
        return (Constraint) super.withId(id);
    }

    @Override
    public Constraint withDescription(String description) {
        return (Constraint) super.withDescription(description);
    }

    @Override
    public Constraint withName(String name) {
        return (Constraint) super.withName(name);
    }

    @Override
    public Constraint withType(String type) {
        return (Constraint) super.withType(type);
    }

    @Override
    public Constraint ofType(String type) {
        return (Constraint) super.ofType(type);
    }

    public Constraint withType(ConstraintType type) {
        return ofType(type);
    }

    public Constraint ofType(ConstraintType type) {
        return (Constraint) super.ofType(type.toString());
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

        private final String name;

        private final String unit;

        Metric(String metric, String unit) {
            this.name = metric;
            this.unit = unit;
        }

        public final String getName() {
            return name;
        }

        public final String getUnit() {
            return unit;
        }


    }

    public enum Operator {

        LessThan("<"),
        GreaterThan(">"),
        Equals("=="),
        And("AND"),
        Or("OR"),
        UNDEF("__UNDEF__");

        private final String operator;

        Operator(String value) {
            this.operator = value;
        }


        @Override
        public String toString() {
            return operator;
        }
    }

    public enum ConstraintType {
        SYBL("SYBLConstraint");

        private final String type;

        ConstraintType(String type) {
            this.type = type;
        }


        @Override
        public String toString() {
            return type;
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

    public String render() {
        return String.format("%s: CONSTRAINT %s %s %s %s", id, metric.name, operator.operator, value, metric.unit);
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "name=" + metric +
                ", operator=" + operator +
                ", operator='" + value + '\'' +
                "} " + super.toString();
    }
}
