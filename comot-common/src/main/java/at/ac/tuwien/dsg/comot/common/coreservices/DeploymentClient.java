package at.ac.tuwien.dsg.comot.common.coreservices;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;

public interface DeploymentClient extends CoreServiceClient {

	public CloudService deploy(
			CloudService cloudService) throws CoreServiceException, ComotException;

	public void undeploy(
			String serviceId) throws CoreServiceException;

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

	public CloudService refreshStatus(
			CloudService cloudService) throws CoreServiceException, ComotException;

	public CloudService getService(String serviceId) throws CoreServiceException, ComotException;

	boolean isRunning(String serviceID) throws CoreServiceException, ComotException;

}
