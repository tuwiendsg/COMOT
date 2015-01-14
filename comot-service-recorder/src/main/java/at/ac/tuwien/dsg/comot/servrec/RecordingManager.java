package at.ac.tuwien.dsg.comot.servrec;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.core.dal.ServiceRepoProxy;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.recorder.revisions.RevisionApi;

@Component
public class RecordingManager {

	private static final Logger log = LoggerFactory.getLogger(RecordingManager.class);

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected ServiceRepoProxy serviceRepo;
	@Autowired
	protected RevisionApi revisionApi;

	protected Map<String, ManagedService> services = new HashMap<>();

	public enum ChangeType {
		INSERTED, STATE
	}

	@Autowired
	protected ToscaMapper mapperTosca;

	public void addService(
			String serviceId,
			DeploymentClient deployment,
			ControlClient control,
			MonitoringClient monitoring) {

		ManagedService service = new ManagedService(serviceId, deployment, control, monitoring);

		services.put(serviceId, service);

	}

	public void removeService(String serviceId) throws ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotException("The service " + serviceId + " is not managed by RecordingManager");
		}

		stopRecording(serviceId);

		services.remove(serviceId);
		// TODO probably also delete data

	}

	public void startRecording(String serviceId) throws ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotException("The service " + serviceId + " is not managed by RecordingManager");
		}

		ServiceEntity entity = serviceRepo.findOne(serviceId);

		if (entity.getRecording() == false) {

			entity.setRecording(true);
			serviceRepo.save(entity);

			ManagedService mService = services.get(serviceId);
			StateChecker stateChecker = context.getBean(StateChecker.class);
			stateChecker.start(mService);

		}

	}

	public void stopRecording(String serviceId) throws ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotException("The service " + serviceId + " is not managed by RecordingManager");
		}

		ServiceEntity entity = serviceRepo.findOne(serviceId);
		entity.setRecording(false); // this will stop all executors
		serviceRepo.save(entity);

	}

	public void insertVersion(CloudService service) throws IllegalArgumentException, IllegalAccessException,
			ComotException {

		if (!services.containsKey(service.getId())) {
			throw new ComotException("The service " + service.getId() + " is not managed by RecordingManager");
		}

		revisionApi.createOrUpdateRegion(service, service.getId(), ChangeType.INSERTED.toString());

	}

	public CloudService getRevision(String serviceId, Long timestamp) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException, ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotException("The service " + serviceId + " is not managed by RecordingManager");
		}

		CloudService service = (CloudService) revisionApi.getRevision(serviceId, serviceId, timestamp);
		return service;
	}

}
