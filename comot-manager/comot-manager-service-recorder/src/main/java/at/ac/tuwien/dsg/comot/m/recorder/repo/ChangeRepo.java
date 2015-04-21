package at.ac.tuwien.dsg.comot.m.recorder.repo;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.m.recorder.model.Change;

public interface ChangeRepo extends GraphRepository<Change> {

	@Query("match (r:_REGION {_id: {0}} )-[:_FIRST_REV]-> ()-[*]->(m:_Revision) "
			+ "match ()-[ch:CHANGE]->(m) where ch.timestamp IN {1} return ch ORDER BY ch.timestamp")
	Iterable<Change> getAllChanges(String regionId);

	
	@Query("match (r:_REGION {_id: {0}} )-[:_FIRST_REV]-> ()-[*]->(m:_Revision) "
			+ "match ()-[ch:CHANGE]->(m) where ch.timestamp IN {1} return ch ORDER BY ch.timestamp")
	Iterable<Change> getAllChangesWithTimestampsOrdered(String regionId, List<Long> timestamps);

	@Query("match (r:_REGION {_id: {0}} )-[:_FIRST_REV]-> ()-[*]->(m:_Revision) "
			+ "match ()-[ch:CHANGE]->(m) where (ch.timestamp >= {1} AND ch.timestamp <= {2}) return ch ORDER BY ch.timestamp")
	Iterable<Change> getAllChangesInRange(String regionId, Long from, Long to);

	@Query("match (r:_REGION {_id: {0}} )-[:_FIRST_REV]-> ()-[*]->(m:_Revision) "
			+ "match ()-[ch:CHANGE]->(m) where ( ch.targetObjectId = {1} AND ch.timestamp >= {2} AND ch.timestamp <= {3} ) return ch ORDER BY ch.timestamp")
	Iterable<Change> getAllChangesInRangeForObject(String regionId, String targetObjectId, Long from, Long to);

	@Query("match (r:_REGION {_id: {0}} )-[:_FIRST_REV]-> ()-[*]->(m:_Revision) "
			+ "match ()-[ch:CHANGE]->(m) where ch.timestamp = {1} return ch")
	Change getByTimestamp(String regionId, Long timestamp);
}
