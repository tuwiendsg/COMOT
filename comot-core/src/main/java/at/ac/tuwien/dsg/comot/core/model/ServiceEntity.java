package at.ac.tuwien.dsg.comot.core.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
public class ServiceEntity {

	@Id
	protected String id;
	protected String name;
	protected Date dateCreated;
	protected Boolean deployment;
	protected Boolean monitoring;
	protected Boolean control;

	@XmlTransient
	@Lob
	protected CloudService serviceOriginal;

	@XmlTransient
	@Lob
	protected CloudService serviceDeployed;

	public ServiceEntity() {
	}

	public ServiceEntity(CloudService serviceOriginal, CloudService serviceDeployed) {
		dateCreated = new Date();
		this.id = serviceOriginal.getId();
		this.name = serviceOriginal.getName();
		this.serviceOriginal = serviceOriginal;
		this.serviceDeployed = serviceDeployed;
		deployment = true;
		monitoring = false;
		control = false;
	}

	// GENERATED METHODS

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public CloudService getServiceOriginal() {
		return serviceOriginal;
	}

	public void setServiceOriginal(CloudService serviceOriginal) {
		this.serviceOriginal = serviceOriginal;
	}

	public CloudService getServiceDeployed() {
		return serviceDeployed;
	}

	public void setServiceDeployed(CloudService serviceDeployed) {
		this.serviceDeployed = serviceDeployed;
	}

	public Boolean getDeployment() {
		return deployment;
	}

	public void setDeployment(Boolean deployment) {
		this.deployment = deployment;
	}

	public Boolean getMonitoring() {
		return monitoring;
	}

	public void setMonitoring(Boolean monitoring) {
		this.monitoring = monitoring;
	}

	public Boolean getControl() {
		return control;
	}

	public void setControl(Boolean control) {
		this.control = control;
	}

}
