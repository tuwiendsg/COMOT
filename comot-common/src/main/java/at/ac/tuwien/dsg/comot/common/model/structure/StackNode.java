package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.node.Capability;
import at.ac.tuwien.dsg.comot.common.model.node.Properties;
import at.ac.tuwien.dsg.comot.common.model.node.Requirement;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;

public class StackNode extends AbstractEntity implements ReferencableEntity {

	public static final int DEFAULT_INSTANCES = 1;

	protected int minInstances = DEFAULT_INSTANCES;
	protected int maxInstances = DEFAULT_INSTANCES;

	protected NodeType type;

	protected List<Requirement> requirements = new ArrayList<>();
	protected List<Capability> capabilities = new ArrayList<>();
	protected List<ArtifactTemplate> deploymentArtifacts = new ArrayList<>();
	protected Properties properties;

	public StackNode() {

	}

	public StackNode(String id, NodeType type) {
		super(id);
		this.type = type;

	}

	public StackNode(String id, String name, int minInstances, int maxInstances, NodeType type) {
		super(id, name);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.type = type;
	}

	public StackNode(String id, String name, int minInstances, int maxInstances, NodeType type,
			List<Requirement> requirements, List<Capability> capabilities,
			Properties properties, List<ArtifactTemplate> deploymentArtifacts) {
		super(id, name);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.type = type;
		this.requirements = requirements;
		this.capabilities = capabilities;
		this.properties = properties;
		this.deploymentArtifacts = deploymentArtifacts;
	}

	public void addDeploymentArtifact(ArtifactTemplate template) {
		if (deploymentArtifacts == null) {
			deploymentArtifacts = new ArrayList<>();
		}
		deploymentArtifacts.add(template);
	}

	public void addRequirement(Requirement requirement) {
		if (requirements == null) {
			requirements = new ArrayList<>();
		}
		requirements.add(requirement);
	}

	public void addCapability(Capability capability) {
		if (capabilities == null) {
			capabilities = new ArrayList<>();
		}
		capabilities.add(capability);
	}

	// GENERATED METHODS

	public int getMinInstances() {
		return minInstances;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
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

	public List<Requirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}

	public List<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
