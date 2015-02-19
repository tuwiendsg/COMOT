package at.ac.tuwien.dsg.comot.m.recorder.cs;

import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.m.recorder.cs.RecordingManager.ChangeType;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

@Component("prototype")
public class StateChecker extends AbstractAsyncExecutor {

	@Override
	protected void oneIteration() throws CoreServiceException, ComotException, IllegalArgumentException,
			IllegalAccessException {

		ServiceEntity entity = serviceRepo.findOne(mService.getServiceId());

		if (entity.getDeployment() == false) {
			return;
		}

		DeploymentClient deployment = mService.getDeployment();
		CloudService service = deployment.getService(mService.getServiceId());
		service = deployment.refreshStatus(service);

		revisionApi.createOrUpdateRegion(service, service.getId(), ChangeType.STATE.toString());

	}

}
