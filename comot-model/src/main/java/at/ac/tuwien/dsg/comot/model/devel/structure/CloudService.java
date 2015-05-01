package at.ac.tuwien.dsg.comot.model.devel.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CloudService extends ServiceEntity {

	private static final long serialVersionUID = 4336434946064439267L;

	@XmlAttribute
	protected String accessIp;
	@XmlAttribute
	protected Long dateCreated;
	@XmlElement(name = "Topology")
	protected Set<ServiceTopology> serviceTopologies = new HashSet<>();
	
	protected Set<OsuInstance> support = new HashSet<>();

	public CloudService() {
	}

	public CloudService(String id) {
		super(id);
	}

	public CloudService(String id, String name, Long dateCreated) {
		super(id, name);
		this.dateCreated = dateCreated;
	}


	public void addServiceTopology(ServiceTopology serviceTopology) {
		if (serviceTopologies == null) {
			serviceTopologies = new HashSet<>();
		}
		serviceTopologies.add(serviceTopology);
	}

	public List<ServiceTopology> getServiceTopologiesList() {
		return new ArrayList<ServiceTopology>(serviceTopologies);
	}
	

	// GENERATED METHODS

	public Set<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public void setServiceTopologies(Set<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	public Long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Set<OsuInstance> getSupport() {
		return support;
	}

	public void setSupport(Set<OsuInstance> support) {
		this.support = support;
	}
	
	

}
