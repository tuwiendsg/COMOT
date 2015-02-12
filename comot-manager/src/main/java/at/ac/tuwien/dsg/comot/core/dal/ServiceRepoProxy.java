package at.ac.tuwien.dsg.comot.core.dal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.core.model.Job;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;

/**
 * !!! Workaround for not functioning DB in comot-service-recorder (runs, but does not save data). By default data is
 * saved to DB. If setFake(true), services are kept only in a Map not in DB.
 * 
 * @author Juraj
 *
 */
@Component
public class ServiceRepoProxy {

	protected final Logger log = LoggerFactory.getLogger(ServiceRepoProxy.class);

	@Autowired
	protected ServiceRepo serviceRepo;

	protected boolean beFake;

	protected Map<String, ServiceEntity> entities = new HashMap<>();
	protected Long jobCounter = 0L;

	// ////

	public ServiceEntity save(ServiceEntity entity) {

		log.trace("PROXY({}) save({}) ", beFake, entity);

		if (beFake) {
			return entities.put(entity.getId(), entity);
		} else {
			return serviceRepo.save(entity);
		}

	}

	public ServiceEntity findOne(String id) {

		log.info("PROXY({}) findOne({}) ", beFake, id);

		if (beFake) {
			return entities.get(id);
		} else {
			return serviceRepo.findOne(id);
		}

	}

	public Iterable<ServiceEntity> findAll() {

		log.info("PROXY({}) findAll() ", beFake);

		if (beFake) {
			return new ArrayList<ServiceEntity>(entities.values());
		} else {
			return serviceRepo.findAll();
		}

	}

	// /////////

	public void save(Job job) {
		job.setId(jobCounter++);

		log.info("job.getService() {}", job.getService());
		log.info("job.getService().getId() {}", job.getService().getId());
		entities.get(job.getService().getId()).addJob(job);

	}

	public Iterable<Job> findAllJobs() {

		List<Job> jobList = new ArrayList<>();

		for (ServiceEntity one : entities.values()) {
			if (one.getJobs() != null) {
				jobList.addAll(one.getJobs());
			}
		}

		return jobList;
	}

	public void delete(Long id) {

		for (ServiceEntity one : entities.values()) {
			for (Job job : one.getJobs()) {
				if (job.getId() == id) {
					one.getJobs().remove(job);
					return;
				}
			}
		}

	}

	public boolean isFake() {
		return beFake;
	}

	public void setFake(boolean beFake) {
		this.beFake = beFake;
	}

}
