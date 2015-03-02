package at.ac.tuwien.dsg.comot.m.ui.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ElementState {

	protected String id;
	protected Integer instanceId;
	protected List<ElementState> children = new ArrayList<>();
	protected boolean serviceUnit;
	protected List<String> connectToIds = new ArrayList<>();

	public ElementState() {

	}

	public ElementState(String id) {
		super();
		this.id = id;
	}

	public void addChild(ElementState element) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(element);
	}

	public void addConnectToId(String id) {
		if (connectToIds == null) {
			connectToIds = new ArrayList<>();
		}
		connectToIds.add(id);
	}

	// GENERATED METHODS

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ElementState> getChildren() {
		return children;
	}

	public void setChildren(List<ElementState> children) {
		this.children = children;
	}

	public boolean isServiceUnit() {
		return serviceUnit;
	}

	public void setServiceUnit(boolean serviceUnit) {
		this.serviceUnit = serviceUnit;
	}

	public Integer getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Integer instanceId) {
		this.instanceId = instanceId;
	}

	public List<String> getConnectToId() {
		return connectToIds;
	}

	public void setConnectToId(List<String> connectToId) {
		this.connectToIds = connectToId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + instanceId;
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
		ElementState other = (ElementState) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (instanceId != other.instanceId)
			return false;
		return true;
	}

}
