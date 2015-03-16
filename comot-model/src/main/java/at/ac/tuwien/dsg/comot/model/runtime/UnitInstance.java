package at.ac.tuwien.dsg.comot.model.runtime;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class UnitInstance implements Serializable, HasUniqueId {

	private static final long serialVersionUID = 6826825251009392073L;

	@GraphId
	protected Long nodeId;
	@XmlID
	@XmlAttribute
	@BusinessId
	protected String id;
	@XmlAttribute
	protected String envId;
	@XmlAttribute
	protected State state;
	@XmlIDREF
	@XmlAttribute
	protected UnitInstance hostInstance;

	public UnitInstance() {

	}

	public UnitInstance(String id, String envId, State state, UnitInstance hostInstance) {
		super();
		this.id = id;
		this.envId = envId;
		this.state = state;
		this.hostInstance = hostInstance;
	}

	// GENERATED METHODS

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public UnitInstance getHostInstance() {
		return hostInstance;
	}

	public void setHostInstance(UnitInstance hostInstance) {
		this.hostInstance = hostInstance;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEnvId() {
		return envId;
	}

	public void setEnvId(String envId) {
		this.envId = envId;
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
		UnitInstance other = (UnitInstance) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	

}
