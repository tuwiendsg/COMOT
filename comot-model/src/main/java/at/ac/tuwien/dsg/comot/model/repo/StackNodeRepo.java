package at.ac.tuwien.dsg.comot.model.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.model.structure.StackNode;

public interface StackNodeRepo extends GraphRepository<StackNode> {

}
