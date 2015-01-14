package at.ac.tuwien.dsg.comot.servrec;

import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.servrec.RecordingManager.ChangeType;

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
