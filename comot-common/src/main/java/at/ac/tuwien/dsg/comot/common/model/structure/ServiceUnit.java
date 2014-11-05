package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.elastic.Capability;
import at.ac.tuwien.dsg.comot.common.model.elastic.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.elastic.Requirement;
import at.ac.tuwien.dsg.comot.common.model.elastic.SyblDirective;

public class ServiceUnit extends ServicePart {

	public static final int DEFAULT_INSTANCES = 1;

	protected int minInstances = DEFAULT_INSTANCES;
	protected int maxInstances = DEFAULT_INSTANCES;
	protected List<ArtifactTemplate> deploymentArtifacts = new ArrayList<>();

	public ServiceUnit() {
	}

	public ServiceUnit(String id) {
		super(id);
	}

	public ServiceUnit(String id, String type, String name, int minInstances, int maxInstances) {
		super(id, type, name);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
	}

	public ServiceUnit(
			String id,
			String type,
			List<SyblDirective> directives,
			List<Requirement> requirements,
			List<Capability> capabilities,
			List<ElasticityCapability> elasticityCapabilities,
			Map<String, Object> properties,
			int minInstances,
			int maxInstances,
			List<ArtifactTemplate> deploymentArtifacts) {
		super(id, type, directives, requirements, capabilities, elasticityCapabilities, properties);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.deploymentArtifacts = deploymentArtifacts;
	}

	public ServiceUnit(
			String id,
			String type,
			String name,
			List<SyblDirective> directives,
			List<Requirement> requirements,
			List<Capability> capabilities,
			List<ElasticityCapability> elasticityCapabilities,
			Map<String, Object> properties,
			int minInstances,
			int maxInstances,
			List<ArtifactTemplate> deploymentArtifacts) {
		super(id, type, name, directives, requirements, capabilities, elasticityCapabilities, properties);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.deploymentArtifacts = deploymentArtifacts;
	}

	public void addDeploymentArtifact(ArtifactTemplate template) {
		if (deploymentArtifacts == null) {
			deploymentArtifacts = new ArrayList<>();
		}
		deploymentArtifacts.add(template);
	}

	// GENERATED METHODS

	public int getMinInstances() {
		return minInstances;
	}

	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}

	public int getMaxInstances() {
		return maxInstances;
	}

	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}

	public List<ArtifactTemplate> getDeploymentArtifacts() {
		return deploymentArtifacts;
	}

	public void setDeploymentArtifacts(List<ArtifactTemplate> deploymentArtifacts) {
		this.deploymentArtifacts = deploymentArtifacts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((deploymentArtifacts == null) ? 0 : deploymentArtifacts.hashCode());
		result = prime * result + maxInstances;
		result = prime * result + minInstances;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceUnit other = (ServiceUnit) obj;
		if (deploymentArtifacts == null) {
			if (other.deploymentArtifacts != null)
				return false;
		} else if (!deploymentArtifacts.equals(other.deploymentArtifacts))
			return false;
		if (maxInstances != other.maxInstances)
			return false;
		if (minInstances != other.minInstances)
			return false;
		return true;
	}

}
