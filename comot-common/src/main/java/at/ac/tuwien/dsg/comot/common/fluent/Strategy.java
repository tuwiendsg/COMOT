package at.ac.tuwien.dsg.comot.common.fluent;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

/**
 * Created by omoser on 3/1/14. todo atm only AND or OR constraints are supported
 */
public class Strategy extends AbstractCloudEntity implements Renderable {

	private Action action;

	private Constraint.Operator operator = Constraint.Operator.UNDEF;

	private Constraint.ConstraintType strategyConstraintType = Constraint.ConstraintType.SYBL;

	private Set<Constraint> constraints = new HashSet<>();

	Strategy(String id) {
		super(id);
	}

	public static Strategy Strategy(String id) {
		return new Strategy(id);
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
			return name;
		}
	}

	//
	// public API
	//

	public Strategy withStrategyType(final Constraint.ConstraintType strategyConstraintType) {
		this.strategyConstraintType = strategyConstraintType;
		return this;
	}

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

	public Action getAction() {
		return action;
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
					+
					"previously: " + this.operator);
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

		return builder.append(Joiner.on(" AND ").join(constraintsToRender))
				.append(" : ")
				.append(action)
				.toString();
	}
}
