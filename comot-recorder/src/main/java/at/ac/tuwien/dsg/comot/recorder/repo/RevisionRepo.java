package at.ac.tuwien.dsg.comot.recorder.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.recorder.model.Revision;

public interface RevisionRepo extends GraphRepository<Revision> {

}
