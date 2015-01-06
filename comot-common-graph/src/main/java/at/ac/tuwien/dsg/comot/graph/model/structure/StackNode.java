package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.comot.graph.model.ConnectToRelationship;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.graph.model.node.NodeInstance;
import at.ac.tuwien.dsg.comot.graph.model.node.Properties;
import at.ac.tuwien.dsg.comot.graph.model.type.NodeType;
import at.ac.tuwien.dsg.comot.graph.model.type.State;

@NodeEntity
public class StackNode implements Serializable {

	private static final long serialVersionUID = 4825578027474573978L;

	@GraphId
	protected Long nodeId;

	protected String id;
	protected String name;
	protected Integer minInstances = 1;
	protected Integer maxInstances = 1;
	protected NodeType type;
	protected State state;

	protected StackNode hostNode;
	@RelatedToVia(type = "CONNECT_TO")
	protected Set<ConnectToRelationship> connectTo = new HashSet<>();

	protected Set<ArtifactTemplate> deploymentArtifacts = new HashSet<>();
	protected Set<Properties> properties = new HashSet<>();
	protected Set<NodeInstance> instances = new HashSet<>();

	public StackNode() {

	}

	public StackNode(String id, NodeType type) {
		this.id = id;
		this.type = type;

	}

	public StackNode(String id, String name, Integer minInstances, Integer maxInstances, NodeType type) {
		this.id = id;
		this.name = name;
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.type = type;
	}

	public StackNode(String id, String name, Integer minInstances, Integer maxInstances, NodeType type,
			Set<Properties> properties, Set<ArtifactTemplate> deploymentArtifacts) {
		this.id = id;
		this.name = name;
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.type = type;
		this.properties = properties;
		this.deploymentArtifacts = deploymentArtifacts;
	}

	public void addDeploymentArtifact(ArtifactTemplate template) {
		if (deploymentArtifacts == null) {
			deploymentArtifacts = new HashSet<>();
		}
		deploymentArtifacts.add(template);
	}

	public void addConnectTo(ConnectToRelationship rel) {
		if (connectTo == null) {
			connectTo = new HashSet<>();
		}
		connectTo.add(rel);
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StackNode getHostNode() {
		return hostNode;
	}

	public void setHostNode(StackNode hostNode) {
		this.hostNode = hostNode;
	}

	public Set<ConnectToRelationship> getConnectTo() {
		return connectTo;
	}

	public void setConnectTo(Set<ConnectToRelationship> connectTo) {
		this.connectTo = connectTo;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

}
