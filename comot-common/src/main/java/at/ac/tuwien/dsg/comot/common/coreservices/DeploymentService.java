package at.ac.tuwien.dsg.comot.common.coreservices;

import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public interface DeploymentService {

	public String deploy(CloudService CloudService) throws CoreServiceException;

	public void undeploy(String serviceId) throws CoreServiceException;

	public void spawn(
			String serviceId,
			String topologyId,
			String nodeId,
			int instanceCount) throws CoreServiceException;

	public void destroy(
			String serviceId,
			String topologyId,
			String nodeId,
			int instanceId) throws CoreServiceException;

	public at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService getStatus(String serviceId)
			throws CoreServiceException;

	public DeploymentDescription getServiceDeploymentInfo(String serviceId) throws CoreServiceException;

	public void close();
}
