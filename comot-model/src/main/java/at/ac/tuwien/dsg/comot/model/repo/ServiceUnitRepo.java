package at.ac.tuwien.dsg.comot.model.repo;

import org.springframework.data.neo4j.repository.GraphRepository;

import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;

public interface ServiceUnitRepo extends GraphRepository<ServiceUnit> {

}
