package at.ac.tuwien.dsg.comot.model.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.model.node.NodeInstance;
import at.ac.tuwien.dsg.comot.model.node.Properties;
import at.ac.tuwien.dsg.comot.model.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.relationship.LocalRel;
import at.ac.tuwien.dsg.comot.model.type.NodeType;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class StackNode implements HasUniqueId, Serializable {

	private static final long serialVersionUID = 4825578027474573978L;

	@GraphId
	protected Long nodeId;
	@XmlID
	@XmlAttribute
	@BusinessId
	protected String id;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected Integer minInstances = 1;
	@XmlAttribute
	protected Integer maxInstances = 1;
	@XmlAttribute
	protected NodeType type;
	@XmlAttribute
	protected State state;

	@RelatedToVia(type = "HOST_ON")
	protected HostOnRel hostNode;

	@RelatedToVia(type = "CONNECT_TO")
	@XmlElementWrapper(name = "Connections")
	@XmlElement(name = "Connection")
	protected Set<ConnectToRel> connectTo = new HashSet<>();

	@RelatedToVia(type = "LOCAL")
	@XmlElementWrapper(name = "LocalRelationships")
	@XmlElement(name = "Local")
	protected Set<LocalRel> local = new HashSet<>();

	@XmlIDREF
	@XmlElementWrapper(name = "Artifacts")
	@XmlElement(name = "Artifact")
	protected Set<ArtifactTemplate> deploymentArtifacts = new HashSet<>();

	@XmlElementWrapper(name = "Properties")
	@XmlElement(name = "Property")
	protected Set<Properties> properties = new HashSet<>();

	@XmlElementWrapper(name = "Instances")
	protected Set<NodeInstance> instances = new HashSet<>();

	public StackNode() {

	}

	public StackNode(String id, NodeType type) {
		setId(id);
		this.type = type;

	}

	public StackNode(String id, String name, Integer minInstances, Integer maxInstances, NodeType type) {
		this(id, type);
		this.name = name;
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
	}

	public void addDeploymentArtifact(ArtifactTemplate template) {
		if (deploymentArtifacts == null) {
			deploymentArtifacts = new HashSet<>();
		}
		deploymentArtifacts.add(template);
	}

	public void addConnectTo(ConnectToRel rel) {
		if (connectTo == null) {
			connectTo = new HashSet<>();
		}
		connectTo.add(rel);
	}

	public void addLocal(LocalRel rel) {
		if (local == null) {
			local = new HashSet<>();
		}
		local.add(rel);
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

	public void setId(String id) {
		this.id = id;
	}

	public List<ConnectToRel> getConnectToList() {
		return new ArrayList<ConnectToRel>(connectTo);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<ConnectToRel> getConnectTo() {
		return connectTo;
	}

	public void setConnectTo(Set<ConnectToRel> connectTo) {
		this.connectTo = connectTo;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public Set<LocalRel> getLocal() {
		return local;
	}

	public void setLocal(Set<LocalRel> local) {
		this.local = local;
	}

	public HostOnRel getHostNode() {
		return hostNode;
	}

	public void setHostNode(HostOnRel hostNode) {
		this.hostNode = hostNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StackNode other = (StackNode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
