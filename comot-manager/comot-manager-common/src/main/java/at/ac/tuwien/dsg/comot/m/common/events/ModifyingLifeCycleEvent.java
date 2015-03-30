package at.ac.tuwien.dsg.comot.m.common.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ModifyingLifeCycleEvent extends LifeCycleEvent {

	private static final long serialVersionUID = 5026486925881972861L;

	@XmlAttribute
	protected String parentId;
	protected ServiceEntity entity;
	protected UnitInstance instance;

	public ModifyingLifeCycleEvent() {

	}

	public ModifyingLifeCycleEvent(
			String serviceId,
			String csInstanceId,
			String groupId,
			Action action,
			String origin,
			Long time,
			String parentId,
			ServiceEntity entity) {
		super(serviceId, csInstanceId, groupId, action, origin, time);
		this.parentId = parentId;
		this.entity = entity;
	}

	public ModifyingLifeCycleEvent(
			String serviceId,
			String csInstanceId,
			String groupId,
			Action action,
			String origin,
			Long time,
			String parentId,
			UnitInstance instance) {
		super(serviceId, csInstanceId, groupId, action, origin, time);
		this.parentId = parentId;
		this.instance = instance;
	}

	public Type targetGroupType() {
		if (instance != null) {
			return Type.INSTANCE;
		} else if (entity != null) {
			if (entity instanceof CloudService) {
				return Type.SERVICE;
			} else if (entity instanceof ServiceTopology) {
				return Type.TOPOLOGY;
			} else if (entity instanceof ServiceUnit) {
				return Type.UNIT;
			}
		}
		return null;
	}

	// GENERATED

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public ServiceEntity getEntity() {
		return entity;
	}

	public void setEntity(ServiceEntity entity) {
		this.entity = entity;
	}

	public UnitInstance getInstance() {
		return instance;
	}

	public void setInstance(UnitInstance instance) {
		this.instance = instance;
	}

	@Override
	public String toString() {
		return "ModifyingLifeCycleEvent [name=" + action + ", serviceId=" + serviceId
				+ ", csInstanceId=" + csInstanceId + ", groupId=" + groupId + ", origin=" + origin + ", parentId="
				+ parentId + "]";
	}

}
