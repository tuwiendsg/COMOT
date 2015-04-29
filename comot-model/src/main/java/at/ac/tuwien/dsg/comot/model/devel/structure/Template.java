package at.ac.tuwien.dsg.comot.model.devel.structure;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class Template implements Serializable {

	private static final long serialVersionUID = 3118685154771386221L;

	@GraphId
	protected Long nodeId;

	@Indexed(unique = true)
	@XmlID
	@XmlAttribute
	protected String id;

	protected CloudService description;

	public Template() {

	}

	public Template(String id, CloudService service) {
		this.id = id;
		this.description = service;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CloudService getDescription() {
		return description;
	}

	public void setDescription(CloudService description) {
		this.description = description;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

}
