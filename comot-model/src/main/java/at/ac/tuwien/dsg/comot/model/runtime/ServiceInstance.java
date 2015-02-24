package at.ac.tuwien.dsg.comot.model.runtime;

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

import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class ServiceInstance {

	@GraphId
	protected Long nodeId;
	@BusinessId
	@XmlAttribute
	protected String id;
	@XmlElementWrapper(name = "UnitInstances")
	@XmlElement(name = "Instance")
	protected Set<UnitInstance> unitInstances = new HashSet<>();
	
	protected Set<OfferedServiceUnit> support = new HashSet<>();;
	
	public ServiceInstance(){
		
	}
	
	public ServiceInstance(String id) {
		super();
		this.id = id;
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

	public Set<OfferedServiceUnit> getSupport() {
		return support;
	}

	public void setSupport(Set<OfferedServiceUnit> support) {
		this.support = support;
	}
	

}
