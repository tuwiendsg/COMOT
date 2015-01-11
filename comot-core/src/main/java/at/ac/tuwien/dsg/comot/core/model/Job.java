package at.ac.tuwien.dsg.comot.core.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Job {

	@XmlTransient
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected Long id;
	@Enumerated(EnumType.STRING)
	protected Type type;

	@XmlTransient
	@ManyToOne
	protected ServiceEntity service;

	public enum Type {
		START_MONITORING, START_CONTROL, UPDATE_STRUCTURE_MONITORING
	}

	public Job() {
	}

	public Job(Type type, ServiceEntity service) {
		super();
		this.type = type;
		this.service = service;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	@Override
	public String toString() {
		return "Job [id=" + id + ", type=" + type + ", service=" + service.getId() + "]";
	}

}