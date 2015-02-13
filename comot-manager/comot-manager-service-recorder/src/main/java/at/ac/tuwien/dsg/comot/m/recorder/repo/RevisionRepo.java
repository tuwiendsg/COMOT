package at.ac.tuwien.dsg.comot.m.recorder.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.m.recorder.model.Revision;

public interface RevisionRepo extends GraphRepository<Revision> {

}
