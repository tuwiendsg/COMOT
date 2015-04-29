/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
