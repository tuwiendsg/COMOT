package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class Constraint extends AbstractCloudEntity implements Renderable {

    private Metric metric;

    private Operator operator;

    private Type constraintType = Type.SYBL;

    private String value;

    Constraint(String id) {
        super(id);
        context.put(id, this);
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

    public Constraint ofConstraintType(final Type constraintType) {
        this.constraintType = constraintType;
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

    public Type getConstraintType() {
        return constraintType;
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

    public enum Type {
        SYBL("SYBLConstraint");

        private final String type;

        Type(String type) {
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
