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

import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class UnitInstance implements Serializable {

	private static final long serialVersionUID = 6826825251009392073L;

	@GraphId
	protected Long nodeId;
	@XmlID
	@XmlAttribute
	@BusinessId
	protected String id;
	@XmlAttribute
	protected Integer instanceId;
	@XmlAttribute
	protected String envId;
	@XmlAttribute
	protected State state;
	@XmlIDREF
	@XmlAttribute
	protected UnitInstance hostInstance;

	public UnitInstance() {

	}

	public UnitInstance(String id, int instanceId, String envId, State state, UnitInstance hostInstance) {
		super();
		this.id = id;
		this.instanceId = instanceId;
		this.envId = envId;
		this.state = state;
		this.hostInstance = hostInstance;
	}

	// GENERATED METHODS

	public Integer getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Integer instanceId) {
		this.instanceId = instanceId;
	}

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
	

}
