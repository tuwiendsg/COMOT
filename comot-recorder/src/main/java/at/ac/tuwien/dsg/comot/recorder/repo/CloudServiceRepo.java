package at.ac.tuwien.dsg.comot.recorder.repo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;

public interface CloudServiceRepo extends GraphRepository<CloudService> {

	@Query("match (n:CloudService) where n.id={id} return n")
	CloudService findById(@Param(value = "id") String id);
}
