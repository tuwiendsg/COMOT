package at.ac.tuwien.dsg.comot.model.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class ArtifactTemplate implements HasUniqueId, Serializable {

	private static final long serialVersionUID = 4681582673568700847L;

	@GraphId
	protected Long nodeId;
	@BusinessId
	@XmlID
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected ArtifactType type;
	@XmlElement(name = "Uri")
	protected Set<String> uris = new HashSet<>();

	public ArtifactTemplate() {
	}

	public ArtifactTemplate(String id, ArtifactType type) {
		this.id = id;
		this.type = type;
	}

	public void addUri(String reference) {
		if (uris == null) {
			uris = new HashSet<String>();
		}
		uris.add(reference);
	}

	public List<String> getUrisList() {
		return new ArrayList<String>(uris);
	}

	// GENERATED METHODS

	public ArtifactType getType() {
		return type;
	}

	public void setType(ArtifactType type) {
		this.type = type;
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

	public Set<String> getUris() {
		return uris;
	}

	public void setUris(Set<String> uris) {
		this.uris = uris;
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
		ArtifactTemplate other = (ArtifactTemplate) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
