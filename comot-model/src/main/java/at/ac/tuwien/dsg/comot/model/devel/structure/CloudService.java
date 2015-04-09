package at.ac.tuwien.dsg.comot.model.devel.structure;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;

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
	@XmlElementWrapper(name = "ServiceInstances")
	@XmlElement(name = "Instance")
	protected Set<ServiceInstance> instances = new HashSet<>();

	public CloudService() {
	}

	public CloudService(String id) {
		super(id);
	}

	public CloudService(String id, String name, Long dateCreated) {
		super(id, name);
		this.dateCreated = dateCreated;
	}

	public ServiceInstance createServiceInstance(String instanceId) {
		ServiceInstance instance = new ServiceInstance(instanceId, System.currentTimeMillis());
		instances.add(instance);
		return instance;
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
	
	public List<ServiceInstance> getInstancesList() {
		return new ArrayList<ServiceInstance>(instances);
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

	public Set<ServiceInstance> getInstances() {
		return instances;
	}

	public void setInstances(Set<ServiceInstance> instances) {
		this.instances = instances;
	}

	public Long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Long dateCreated) {
		this.dateCreated = dateCreated;
	}

}
