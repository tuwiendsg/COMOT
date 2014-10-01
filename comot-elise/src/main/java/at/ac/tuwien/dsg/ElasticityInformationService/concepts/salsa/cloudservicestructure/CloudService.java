package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;

public class CloudService extends Entity {
	List<ServiceTopology> topologies;

	public CloudService(){		
	}
	
	public List<ServiceTopology> getTopologyList() {
		return topologies;
	}
	
//	public ServiceTopology getTopologyById(Long topologyId){
//		for (ServiceTopology topo : topologies) {
//			if (topo.getId().equals(topologyId)){
//				return topo;
//			}
//		}
//		return null;
//	}
	
	public void addTopology(ServiceTopology topo){
		if (topologies==null) {
			topologies = new ArrayList<>();
		}
		this.topologies.add(topo);
	}
	
	public void removeTopology(ServiceTopology topo){
		this.topologies.remove(topo);
	}
}
