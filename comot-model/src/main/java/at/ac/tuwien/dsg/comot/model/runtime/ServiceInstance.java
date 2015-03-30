package at.ac.tuwien.dsg.comot.model.runtime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class ServiceInstance implements Serializable {

	private static final long serialVersionUID = 5032108831476284465L;

	@GraphId
	protected Long nodeId;
	@BusinessId
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected Long dateCreated;
	@XmlElementWrapper(name = "UnitInstances")
	@XmlElement(name = "Instance")
	protected Set<UnitInstance> unitInstances = new HashSet<>();

	protected Set<OsuInstance> support = new HashSet<>();;

	public ServiceInstance() {

	}

	public ServiceInstance(String id) {
		super();
		this.id = id;
	}

	public ServiceInstance(String id, Long dateCreated) {
		super();
		this.id = id;
		this.dateCreated = dateCreated;
	}

	public Set<UnitInstance> getUnitInstances() {
		return unitInstances;
	}

	public void setUnitInstances(Set<UnitInstance> unitInstances) {
		this.unitInstances = unitInstances;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public Set<OsuInstance> getSupport() {
		return support;
	}

	public void setSupport(Set<OsuInstance> support) {
		this.support = support;
	}

	public Long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Long dateCreated) {
		this.dateCreated = dateCreated;
	}

}
