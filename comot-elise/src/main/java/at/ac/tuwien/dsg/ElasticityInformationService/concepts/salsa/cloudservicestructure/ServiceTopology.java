package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.ServiceEntity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;

@NodeEntity
public class ServiceTopology extends ServiceEntity {
	private static final long serialVersionUID = 1033006439232497964L;

	@RelatedTo(type=LinkType.SERVICE_TOPOLOGY_CONTAIN_SERVICE_UNIT, direction=Direction.OUTGOING)
	List<ServiceUnit> serviceUnits = new ArrayList<>();
	
	@RelatedTo(type=LinkType.SERVICE_TOPOLOGY_CONTAIN_SERVICE_TOPOLOGY, direction=Direction.OUTGOING)
	List<ServiceTopology> topologies = new ArrayList<>();
	
	public ServiceTopology(){
	}
	
	public void addServiceUnit(ServiceUnit serviceUnit){
		if (serviceUnits==null){
			serviceUnits = new ArrayList<>();
		}
		this.serviceUnits.add(serviceUnit);
	}
	
//	public ServiceUnit getServiceUnitById(Long id){
//		for (ServiceUnit node : serviceUnits) {
//			if (node.getId().equals(id)){
//				return node;
//			}
//		}
//		return null;
//	}
	
	public void removeServiceUnit(ServiceUnit su){
		this.serviceUnits.remove(su);
	}

	public List<ServiceUnit> getServiceUnits() {
		return serviceUnits;
	}
	
	
	
}
