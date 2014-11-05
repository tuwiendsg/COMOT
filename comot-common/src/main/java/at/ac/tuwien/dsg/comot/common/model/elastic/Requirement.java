package at.ac.tuwien.dsg.comot.common.model.elastic;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;

/**
 * @author omoser
 */
public class Requirement extends AbstractEntity {

	public Requirement() {
	}

	public Requirement(String id) {
		super(id);
	}

	public Requirement(String id, String type) {
		super(id);
		setType(type);
	}

	public enum RequirementType {

		Variable("variable");

		private final String type;

		RequirementType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

}
