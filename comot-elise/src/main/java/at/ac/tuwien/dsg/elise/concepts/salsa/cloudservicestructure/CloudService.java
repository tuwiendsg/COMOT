package at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;

@NodeEntity
public class CloudService extends ServiceEntity {
	private static final long serialVersionUID = 6885494187731318894L;
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

	@Override
	public String toString() {
		return "CloudService {"+ topologies + name +  type + primitiveOperations + "}";
	}
	
	
}
