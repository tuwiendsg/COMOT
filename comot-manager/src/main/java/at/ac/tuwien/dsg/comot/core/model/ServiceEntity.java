package at.ac.tuwien.dsg.comot.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
public class ServiceEntity implements Serializable {

	private static final long serialVersionUID = 6899699954958282016L;

	@Id
	protected String id;
	protected String name;
	protected Date dateCreated;

	protected Boolean deployment;
	protected Boolean monitoring;
	protected Boolean control;
	protected Boolean recording;

	@XmlTransient
	@Lob
	protected CloudService serviceDeployed;

	@XmlTransient
	@Lob
	protected CompositionRulesConfiguration mcr;
	@XmlTransient
	@Lob
	protected String effects;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "service")
	protected List<Job> jobs;

	public ServiceEntity() {
	}

	public ServiceEntity(CloudService serviceDeployed) {
		dateCreated = new Date();
		this.id = serviceDeployed.getId();
		this.name = serviceDeployed.getName();
		this.serviceDeployed = serviceDeployed;
		deployment = true;
		monitoring = false;
		control = false;
		recording = false;
	}

	public void addJob(Job job) {
		if (jobs == null) {
			jobs = new ArrayList<>();
		}
		jobs.add(job);
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

	public CompositionRulesConfiguration getMcr() {
		return mcr;
	}

	public void setMcr(CompositionRulesConfiguration mcr) {
		this.mcr = mcr;
	}

	public String getEffects() {
		return effects;
	}

	public void setEffects(String effects) {
		this.effects = effects;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public Boolean getRecording() {
		return recording;
	}

	public void setRecording(Boolean recording) {
		this.recording = recording;
	}

	@Override
	public String toString() {
		return "ServiceEntity [id=" + id + ", name=" + name + ", dateCreated=" + dateCreated + ", deployment="
				+ deployment + ", monitoring=" + monitoring + ", control=" + control + ", recording=" + recording
				+ ", serviceDeployed="
				+ ((serviceDeployed == null) ? null : "data") + ", mcr=" + ((mcr == null) ? null : "data")
				+ ", effects=" + ((effects == null) ? null : "data") + "]";
	}

}
