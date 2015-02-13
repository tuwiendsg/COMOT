package at.ac.tuwien.dsg.comot.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.comot.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.model.node.Properties;
import at.ac.tuwien.dsg.comot.model.node.UnitInstance;
import at.ac.tuwien.dsg.comot.model.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.relationship.LocalRel;
import at.ac.tuwien.dsg.comot.model.type.NodeType;

@XmlRootElement
public class ServiceUnit extends ServiceEntity {

	private static final long serialVersionUID = -1213074714671448573L;

	@XmlAttribute
	protected Integer minInstances = 1;
	@XmlAttribute
	protected Integer maxInstances = 1;
	@XmlAttribute
	protected NodeType type;

	@XmlElementWrapper(name = "ElasticityCapabilities")
	@XmlElement(name = "Capability")
	protected Set<ElasticityCapability> elasticityCapabilities = new HashSet<>();
	@RelatedToVia(type = "HOST_ON")
	protected HostOnRel host;

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
	protected Set<UnitInstance> instances = new HashSet<>();

	public ServiceUnit() {
		super();
	}

	public ServiceUnit(String id) {
		this.id = id;
	}

	public ServiceUnit(String id, String name, Integer minInstances, Integer maxInstances, NodeType type) {
		super(id, name);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.type = type;
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

	public void addNodeInstance(UnitInstance instance) {
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

	public UnitInstance getInstance(Integer instanceId) {
		for (UnitInstance one : instances) {
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

	public List<ArtifactTemplate> getDeploymentArtifactsList() {
		return new ArrayList<ArtifactTemplate>(deploymentArtifacts);
	}

	// GENERATED METHODS

	public Set<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}

	public void setElasticityCapabilities(Set<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}

	public Integer getMinInstances() {
		return minInstances;
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

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public HostOnRel getHost() {
		return host;
	}

	public void setHost(HostOnRel hostNode) {
		this.host = hostNode;
	}

	public Set<ConnectToRel> getConnectTo() {
		return connectTo;
	}

	public void setConnectTo(Set<ConnectToRel> connectTo) {
		this.connectTo = connectTo;
	}

	public Set<LocalRel> getLocal() {
		return local;
	}

	public void setLocal(Set<LocalRel> local) {
		this.local = local;
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

	public Set<UnitInstance> getInstances() {
		return instances;
	}

	public void setInstances(Set<UnitInstance> instances) {
		this.instances = instances;
	}

}
