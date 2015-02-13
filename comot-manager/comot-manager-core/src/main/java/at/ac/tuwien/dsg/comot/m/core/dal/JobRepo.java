package at.ac.tuwien.dsg.comot.m.core.dal;

import org.springframework.data.repository.CrudRepository;

import at.ac.tuwien.dsg.comot.m.core.model.Job;

public interface JobRepo extends CrudRepository<Job, Long> {

}
