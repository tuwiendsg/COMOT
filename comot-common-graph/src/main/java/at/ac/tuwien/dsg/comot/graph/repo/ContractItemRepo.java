package at.ac.tuwien.dsg.comot.graph.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.graph.model.ContractItem;
import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;

public interface ContractItemRepo extends GraphRepository<ContractItem> {

}
