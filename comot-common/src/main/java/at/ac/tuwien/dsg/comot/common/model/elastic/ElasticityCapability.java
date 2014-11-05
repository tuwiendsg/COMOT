package at.ac.tuwien.dsg.comot.common.model.elastic;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;

public class ElasticityCapability extends AbstractEntity {

	private String endpoint;

	public ElasticityCapability() {
	}

	public ElasticityCapability(String id) {
		super(id);
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public enum CapabilityType {

		ScaleIn("scalein"),
		ScaleOut("scaleout");

		private final String type;

		CapabilityType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

}
