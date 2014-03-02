package at.ac.tuwien.dsg.comot.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by omoser on 3/1/14.
 * todo atm only AND or OR constraints are supported (see ConstraintTree below for what should be used)
 */
public class ServiceStrategy extends AbstractCloudEntity {

    private Set<Action> actions = new HashSet<>();

    private ServiceConstraint.Operator operator = ServiceConstraint.Operator.UNDEF;

    private Set<ServiceConstraint> constraints;


    public ServiceStrategy withActions(final Set<Action> actions) {
        this.actions = actions;
        return this;
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

    public ServiceStrategy addAction(Action action) {
        actions.add(action);
        return this;
    }

    public ServiceStrategy withConstraint(ServiceConstraint constraint) {
        constraints.add(constraint);
        return this;
    }

    public ServiceStrategy and(ServiceConstraint constraint) {
        if (operator != ServiceConstraint.Operator.And && operator != ServiceConstraint.Operator.UNDEF) {
            throw new IllegalStateException("Cannot 'and' constraint since another operator was used previously: " + operator);
        }

        operator = ServiceConstraint.Operator.And;
        constraints.add(constraint);
        return this;
    }

    public ServiceStrategy or(ServiceConstraint constraint) {
        if (operator != ServiceConstraint.Operator.Or && operator != ServiceConstraint.Operator.UNDEF) {
            throw new IllegalStateException("Cannot 'or' constraint since another operator was used previously: " + operator);
        }

        operator = ServiceConstraint.Operator.Or;
        constraints.add(constraint);
        return this;
    }

    public ServiceStrategy andConstraints(ServiceConstraint constraintA, ServiceConstraint constraintB) {

        return this;
    }

   /* private class ConstraintTree {

        private ConstraintNode root;

        public ConstraintTree withRoot(final ConstraintNode root) {
            this.root = root;
            return this;
        }

        public ConstraintTree addNode(ConstraintNode node) {
            return this;
        }

        @Override
        public String toString() {
            return "ConstraintTree{" +
                    "root=" + root +
                    '}';
        }
    }

    private class ConstraintNode {

        private ServiceConstraint.Operator operator;

        private ConstraintNode left;

        private ConstraintNode right;

        public ConstraintNode withOperator(final ServiceConstraint.Operator operator) {
            this.operator = operator;
            return this;
        }

        public ConstraintNode withLeft(final ConstraintNode left) {
            this.left = left;
            return this;
        }

        public ConstraintNode withRight(final ConstraintNode right) {
            this.right = right;
            return this;
        }

        @Override
        public String toString() {
            return "ConstraintNode{" +
                    "operator=" + operator +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    private class ConstraintLeave {

        private ServiceConstraint constraint;

        public ConstraintLeave withConstraint(final ServiceConstraint constraint) {
            this.constraint = constraint;
            return this;

        }

        @Override
        public String toString() {
            return "ConstraintLeave{" +
                    "constraint=" + constraint +
                    '}';
        }
    }*/
}
