package at.ac.tuwien.dsg.comot.common.coreservices;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;

public interface DeploymentClient extends CoreServiceClient {

	public String deploy(
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

	public CloudService getStatus(
			CloudService cloudService) throws CoreServiceException, ComotException;

	boolean isRunning(String serviceID) throws CoreServiceException, ComotException;

	// TODO only temporary
	String getStatusGui(String serviceId) throws CoreServiceException;

}
