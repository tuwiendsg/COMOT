package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.graph.model.node.Capability;
import at.ac.tuwien.dsg.comot.graph.model.node.Properties;
import at.ac.tuwien.dsg.comot.graph.model.node.Requirement;
import at.ac.tuwien.dsg.comot.graph.model.type.NodeType;
import at.ac.tuwien.dsg.comot.graph.model.type.State;
import at.ac.tuwien.dsg.comot.graph.model.unit.NodeInstance;

public class StackNode extends AbstractEntity {

	private static final long serialVersionUID = 4825578027474573978L;

	protected Integer minInstances = 1;
	protected Integer maxInstances = 1;

	protected NodeType type;

	protected Set<Requirement> requirements = new HashSet<>();
	protected Set<Capability> capabilities = new HashSet<>();
	protected Set<ArtifactTemplate> deploymentArtifacts = new HashSet<>();
	protected Set<Properties> properties = new HashSet<>();
	protected Set<NodeInstance> instances = new HashSet<>();
	protected State state;
	
	protected StackNode hostsOn;

	public StackNode() {

	}

	public StackNode(String id, NodeType type) {
		super(id);
		this.type = type;

	}

	public StackNode(String id, String name, Integer minInstances, Integer maxInstances, NodeType type) {
		super(id, name);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.type = type;
	}

	public StackNode(String id, String name, Integer minInstances, Integer maxInstances, NodeType type,
			Set<Requirement> requirements, Set<Capability> capabilities,
			Set<Properties> properties, Set<ArtifactTemplate> deploymentArtifacts) {
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
			deploymentArtifacts = new HashSet<>();
		}
		deploymentArtifacts.add(template);
	}

	public void addRequirement(Requirement requirement) {
		if (requirements == null) {
			requirements = new HashSet<>();
		}
		requirements.add(requirement);
	}

	public void addCapability(Capability capability) {
		if (capabilities == null) {
			capabilities = new HashSet<>();
		}
		capabilities.add(capability);
	}

	public void addNodeInstance(NodeInstance instance) {
		if (instances == null) {
			instances = new HashSet<>();
		}
		instances.add(instance);
	}

	public void addProperties(Properties property) {
		if (properties == null) {
			properties = new HashSet<>();
		}
		properties.add(property);
	}

	public NodeInstance getInstance(Integer instanceId) {
		for (NodeInstance one : instances) {
			if (one.getInstanceId() == instanceId) {
				return one;
			}
		}
		return null;
	}

	// GENERATED METHODS

	public Integer getMinInstances() {
		return minInstances;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public void setMinInstances(Integer minInstances) {
		this.minInstances = minInstances;
	}

	public Integer getMaxInstances() {
		return maxInstances;
	}

	public void setMaxInstances(Integer maxInstances) {
		this.maxInstances = maxInstances;
	}

	public Set<ArtifactTemplate> getDeploymentArtifacts() {
		return deploymentArtifacts;
	}

	public void setDeploymentArtifacts(Set<ArtifactTemplate> deploymentArtifacts) {
		this.deploymentArtifacts = deploymentArtifacts;
	}

	public Set<Requirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(Set<Requirement> requirements) {
		this.requirements = requirements;
	}

	public Set<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public Set<Properties> getProperties() {
		return properties;
	}

	public void setProperties(Set<Properties> properties) {
		this.properties = properties;
	}

	public Set<NodeInstance> getInstances() {
		return instances;
	}

	public void setInstances(Set<NodeInstance> instances) {
		this.instances = instances;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public StackNode getHostsOn() {
		return hostsOn;
	}

	public void setHostsOn(StackNode hostsOn) {
		this.hostsOn = hostsOn;
	}
	
	
	

}
