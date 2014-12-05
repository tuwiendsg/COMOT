package at.ac.tuwien.dsg.comot.graph.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.graph.model.node.Requirement;

public interface RequirementRepo extends GraphRepository<Requirement> {

}
