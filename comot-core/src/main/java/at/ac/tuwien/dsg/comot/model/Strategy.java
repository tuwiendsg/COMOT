package at.ac.tuwien.dsg.comot.model;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by omoser on 3/1/14.
 * todo atm only AND or OR constraints are supported
 */
public class Strategy extends AbstractCloudEntity {

    private Action action;

    private Constraint.Operator operator = Constraint.Operator.UNDEF;

    private Set<Constraint> constraints;

    public Strategy(String id) {
        super(id);
    }


    public enum Action {
        ScaleIn("scalein", "enacts a scale-in operation on the platform"),
        ScaleOut("scaleout", "enacts a scale-out operation on the platform");

        private String description;

        private String name;

        Action(String name, String description) {
            this.description = description;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Action{" +
                    "description='" + description + '\'' +
                    ", name='" + name + '\'' +
                    "} " + super.toString();
        }
    }


    //
    // public API
    //

    public Strategy withAction(Action action) {
        this.action = action;
        return this;
    }

    public Strategy then(Action action) {
        return withAction(action);
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

    private void checkOperatorState(Constraint.Operator operator) {
        if (this.operator != operator && operator != Constraint.Operator.UNDEF) {
            throw new IllegalStateException("Cannot add '" + operator + "' constraint since another operator was used " +
                    "previously: " + this.operator);
        }
    }

}
