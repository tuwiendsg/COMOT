package at.ac.tuwien.dsg.comot.common.model;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;

import java.util.*;

import static java.lang.String.format;

/**
 * Created by omoser on 3/1/14. todo atm only AND or OR constraints are
 * supported
 */
public class Strategy extends AbstractCloudEntity implements Renderable,Comparable<Strategy> {

    private ElasticityCapability capability;

    private Constraint.Operator operator = Constraint.Operator.UNDEF;

    private Constraint.ConstraintType strategyConstraintType = Constraint.ConstraintType.SYBL;

    private Set<Constraint> constraints = new HashSet<>();

    Strategy(String id) {
        super(id);
    }

    public static Strategy Strategy(String id) {
        return new Strategy(id);
    }

//    public enum Action {
//        ScaleIn("scalein", "enacts a scale-in operation on the platform"),
//        ScaleOut("scaleout", "enacts a scale-out operation on the platform");
//
//        private String description;
//
//        private String name;
//
//        Action(String name, String description) {
//            this.description = description;
//            this.name = name;
//        }
//
//        @Override
//        public String toString() {
//           return name;
//        }
//    }
    //
    // public API
    //
    public Strategy withStrategyType(final Constraint.ConstraintType strategyConstraintType) {
        this.strategyConstraintType = strategyConstraintType;
        return this;
    }

    public Strategy withCapability(ElasticityCapability capability) {
        this.capability = capability;
        return this;
    }

    public Strategy enforce(ElasticityCapability capability) {
        return withCapability(capability);
    }

    public Strategy when(Constraint constraint) {
        constraints.add(constraint);
        return this;
    }

    public Strategy and(Constraint... constraint) {
        checkOperatorState(Constraint.Operator.And);
        constraints.addAll(Arrays.asList(constraint));
        return this;
    }

    public Strategy and(Constraint constraint) {
        return addConstraint(constraint, Constraint.Operator.And);
    }

    public Strategy or(Constraint... constraints) {
        checkOperatorState(Constraint.Operator.Or);
        this.operator = Constraint.Operator.Or;
        return this;
    }

    public Strategy or(Constraint constraint) {
        return addConstraint(constraint, Constraint.Operator.Or);
    }

    private Strategy addConstraint(Constraint constraint, Constraint.Operator operator) {
        checkOperatorState(operator);
        this.operator = operator;
        constraints.add(constraint);
        return this;
    }

    public ElasticityCapability getCapability() {
        return capability;
    }

    public Constraint.Operator getOperator() {
        return operator;
    }

    public Constraint.ConstraintType getStrategyConstraintType() {
        return strategyConstraintType;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    private void checkOperatorState(Constraint.Operator operator) {
        if (this.operator != operator && this.operator != Constraint.Operator.UNDEF) {
            throw new IllegalStateException("Cannot add '" + operator + "' constraint since another operator was used "
                    + "previously: " + this.operator);
        }
    }

    @Override
    public String render() {
        StringBuilder builder = new StringBuilder(format("%s: STRATEGY CASE ", id));
        List<String> constraintsToRender = new ArrayList<>();
        for (Constraint constraint : this.constraints) {
            Constraint.Metric metric = constraint.getMetric();
            Constraint.Operator operator = constraint.getOperator();
            constraintsToRender.add(
                    format("%s %s %s %s", metric.getName(), operator, constraint.getValue(), metric.getUnit())
            );
        }
        builder.append(Joiner.on(" AND ").join(constraintsToRender))
                .append(" : ");

        if (!capability.getPrimitiveOperations().isEmpty()) {
            String actions = "";
            for (String primitive : capability.getPrimitiveOperations()) {
                actions += primitive + ",";
            }
            actions = actions.substring(0, actions.length() - 1);
            builder.append(actions);
        }else{
             builder.append(capability.type);
        }
        
        return builder.toString();
        
    }

    @Override
    public boolean equals(Object object) {
        return ((object instanceof Strategy) && (id.equalsIgnoreCase(((Strategy) object).id)));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public int compareTo(Strategy o) {
    return ComparisonChain.start()
         .compare(id, o.id)
            .result();
    }
}
