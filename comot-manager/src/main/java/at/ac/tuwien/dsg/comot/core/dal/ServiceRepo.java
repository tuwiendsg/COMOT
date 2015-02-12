package at.ac.tuwien.dsg.comot.core.dal;

import org.springframework.data.repository.CrudRepository;

import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;

public interface ServiceRepo extends CrudRepository<ServiceEntity, String> {

}
