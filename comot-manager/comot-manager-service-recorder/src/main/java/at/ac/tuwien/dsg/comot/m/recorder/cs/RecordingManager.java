package at.ac.tuwien.dsg.comot.m.recorder.cs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.dal.ServiceRepoProxy;
import at.ac.tuwien.dsg.comot.m.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

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
			throw new ComotIllegalArgumentException("The service " + serviceId + " is not managed by RecordingManager");
		}

		stopRecording(serviceId);

		services.remove(serviceId);
		// TODO probably also delete data

	}

	public void startRecording(String serviceId) throws ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("The service " + serviceId + " is not managed by RecordingManager");
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
			throw new ComotIllegalArgumentException("The service " + serviceId + " is not managed by RecordingManager");
		}

		ServiceEntity entity = serviceRepo.findOne(serviceId);
		entity.setRecording(false); // this will stop all executors
		serviceRepo.save(entity);

	}

	public void insertVersion(CloudService service) throws IllegalArgumentException, IllegalAccessException,
			ComotException {

		if (!services.containsKey(service.getId())) {
			throw new ComotIllegalArgumentException("The service " + service.getId()
					+ " is not managed by RecordingManager");
		}

		revisionApi.createOrUpdateRegion(service, service.getId(), ChangeType.INSERTED.toString());

	}

	public Object getRevision(String serviceId, String objectId, Long timestamp) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException, ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("The service " + serviceId + " is not managed by RecordingManager");
		}

		if (!revisionApi.verifyObject(serviceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + serviceId + " there is no managed object "
					+ objectId);
		}

		Object obj = revisionApi.getRevision(serviceId, objectId, timestamp);

		if (obj == null) {
			throw new ComotIllegalArgumentException("There is no revision of service" + serviceId + ", object="
					+ objectId + " at time=" + timestamp + " ");

		}

		return obj;
	}

	public List<Change> getAllChanges(String serviceId, String objectId, Long from, Long to)
			throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException, ComotException {

		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("The service " + serviceId + " is not managed by RecordingManager");
		}

		if (!revisionApi.verifyObject(serviceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + serviceId + " there is no managed object "
					+ objectId);
		}

		Change change = revisionApi.getAllChanges(serviceId, objectId, from, to);

		List<Change> list = new ArrayList<>();

		while (change != null) {
			list.add(change);
			change = change.getTo().getEnd();
		}

		return list;
	}

	public List<ManagedObject> getManagedObjects(String serviceId) {
		if (!services.containsKey(serviceId)) {
			throw new ComotIllegalArgumentException("The service " + serviceId + " is not managed by RecordingManager");
		}

		List<ManagedObject> list = revisionApi.getManagedObjects(serviceId);
		return list;

	}

}
