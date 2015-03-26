package at.ac.tuwien.dsg.comot.model.devel.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.comot.model.devel.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.devel.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.devel.relationship.LocalRel;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@XmlRootElement
public class ServiceUnit extends ServiceEntity {

	private static final long serialVersionUID = -1213074714671448573L;

	@XmlAttribute
	protected Integer minInstances = 1;
	@XmlAttribute
	protected Integer maxInstances = 1;

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

	@XmlElement(name = "OsuInstance")
	protected OsuInstance osuInstance;

	@XmlElementWrapper(name = "Instances")
	protected Set<UnitInstance> instances = new HashSet<>();

	public ServiceUnit() {
		super();
	}

	public ServiceUnit(String id) {
		this.id = id;
	}

	public ServiceUnit(String id, String name, Integer minInstances,
			Integer maxInstances) {
		super(id, name);
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
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

	public void addUnitInstance(UnitInstance instance) {
		if (instances == null) {
			instances = new HashSet<>();
		}
		instances.add(instance);
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

	public void setMinInstances(Integer minInstances) {
		this.minInstances = minInstances;
	}

	public Integer getMaxInstances() {
		return maxInstances;
	}

	public void setMaxInstances(Integer maxInstances) {
		this.maxInstances = maxInstances;
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

	public Set<UnitInstance> getInstances() {
		return instances;
	}

	public void setInstances(Set<UnitInstance> instances) {
		this.instances = instances;
	}

	public OsuInstance getOsuInstance() {
		return osuInstance;
	}

	public void setOsuInstance(OsuInstance osuInstance) {
		this.osuInstance = osuInstance;
	}

}
