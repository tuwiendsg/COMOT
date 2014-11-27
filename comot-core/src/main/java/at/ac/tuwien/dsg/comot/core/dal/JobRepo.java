package at.ac.tuwien.dsg.comot.core.dal;

import org.springframework.data.repository.CrudRepository;

import at.ac.tuwien.dsg.comot.core.model.Job;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;

public interface JobRepo extends CrudRepository<Job, Long> {

}
