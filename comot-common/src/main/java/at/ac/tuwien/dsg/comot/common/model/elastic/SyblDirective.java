package at.ac.tuwien.dsg.comot.common.model.elastic;

import at.ac.tuwien.dsg.comot.common.fluent.Constraint;
import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;

public class SyblDirective extends AbstractEntity {

	protected String directive;

	protected Constraint.ConstraintType strategyConstraintType = Constraint.ConstraintType.SYBL;

	public SyblDirective() {
	}

	public SyblDirective(String id) {
		super(id);
	}

	public SyblDirective(String id, String directive) {
		super(id);
		this.directive = directive;
		type = strategyConstraintType.toString();
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

	public Constraint.ConstraintType getStrategyConstraintType() {
		return strategyConstraintType;
	}

	public String getDirective() {
		return directive;
	}

	public void setDirective(String directive) {
		this.directive = directive;
	}

}
