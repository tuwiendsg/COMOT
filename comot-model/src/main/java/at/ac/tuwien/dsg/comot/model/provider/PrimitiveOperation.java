package at.ac.tuwien.dsg.comot.model.provider;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class PrimitiveOperation implements Serializable {

	private static final long serialVersionUID = -1082005301481421855L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	@XmlAttribute
	protected String id;

	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected String executeMethod;
	@XmlAttribute
	protected String endpoint;

	public PrimitiveOperation() {

	}

	public PrimitiveOperation( String name, String executeMethod) {
		super();
		this.name = name;
		this.executeMethod = executeMethod;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExecuteMethod() {
		return executeMethod;
	}

	public void setExecuteMethod(String executeMethod) {
		this.executeMethod = executeMethod;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
