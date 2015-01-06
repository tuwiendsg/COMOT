package at.ac.tuwien.dsg.comot.recorder.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.recorder.revisions.Change;

public interface ChangeRepo extends GraphRepository<Change> {

}
