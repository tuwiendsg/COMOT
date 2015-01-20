package at.ac.tuwien.dsg.comot.recorder.repo;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.recorder.model.Change;

public interface ChangeRepo extends GraphRepository<Change> {

	@Query("match (r:_REGION {_id: {0}} )-[:_FIRST_REV]-> ()-[*]->(m:_Revision) "
			+ "match ()-[ch:CHANGE]->(m) where ch.timestamp IN {1} return ch ORDER BY ch.timestamp")
	Iterable<Change> getAllChangesWithTimestampsOrdered(String regionId, List<Long> timestamps);

}
