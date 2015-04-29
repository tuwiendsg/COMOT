package at.ac.tuwien.dsg.comot.m.common.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LifeCycleEventModifying extends LifeCycleEvent {

	private static final long serialVersionUID = 5026486925881972861L;

	@XmlAttribute
	protected String parentId;
	protected ServiceEntity entity;
	protected UnitInstance instance;

	public LifeCycleEventModifying() {

	}

	public LifeCycleEventModifying(
			String serviceId,
			String groupId,
			Action action,
			String parentId,
			ServiceEntity entity) {
		super(serviceId, groupId, action);
		this.parentId = parentId;
		this.entity = entity;
	}

	public LifeCycleEventModifying(
			String serviceId,
			String groupId,
			Action action,
			String parentId,
			UnitInstance instance) {
		super(serviceId, groupId, action);
		this.parentId = parentId;
		this.instance = instance;
	}

	public LifeCycleEventModifying(
			String serviceId,
			String groupId,
			Action action,
			String origin,
			Long time,
			String parentId,
			ServiceEntity entity) {
		super(serviceId, groupId, action, origin, time);
		this.parentId = parentId;
		this.entity = entity;
	}

	public LifeCycleEventModifying(
			String serviceId,
			String groupId,
			Action action,
			String origin,
			Long time,
			String parentId,
			UnitInstance instance) {
		super(serviceId, groupId, action, origin, time);
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
				+ ", groupId=" + groupId + ", origin=" + origin + ", parentId="
				+ parentId + "]";
	}

}
