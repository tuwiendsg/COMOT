package at.ac.tuwien.dsg.comot.m.common.coreservices;

import java.util.Map;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public interface DeploymentClient extends ElasticPlatformServiceClient {

	public CloudService deploy(
			CloudService service) throws CoreServiceException, ComotException;

	public CloudService deploy(
			String service) throws CoreServiceException, ComotException;

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
			CloudService service) throws CoreServiceException, ComotException;

	public CloudService refreshStatus(
			Map<String, String> map,
			CloudService service) throws CoreServiceException, ComotException;

	public boolean isManaged(String serviceId) throws CoreServiceException;

	public CloudService getService(String serviceId) throws CoreServiceException, ComotException;

	boolean isRunning(String serviceID) throws CoreServiceException, ComotException;

}
