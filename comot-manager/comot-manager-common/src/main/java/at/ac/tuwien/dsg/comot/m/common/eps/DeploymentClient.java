package at.ac.tuwien.dsg.comot.m.common.eps;

import java.util.Map;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public interface DeploymentClient extends ElasticPlatformServiceClient {

	public CloudService deploy(
			CloudService service) throws EpsException, ComotException;

	public CloudService deploy(
			String service) throws EpsException, ComotException;

	public void undeploy(
			String serviceId) throws EpsException;

	public void spawn(
			String serviceId,
			String topologyId,
			String nodeId,
			int instanceCount) throws EpsException;

	public void destroy(
			String serviceId,
			String topologyId,
			String nodeId,
			int instanceId) throws EpsException;

	public CloudService refreshStatus(
			CloudService service) throws EpsException, ComotException;

	public CloudService refreshStatus(
			Map<String, String> map,
			CloudService service) throws EpsException, ComotException;

	public boolean isManaged(String serviceId) throws EpsException;

	public CloudService getService(String serviceId) throws EpsException, ComotException;

	boolean isRunning(String serviceID) throws EpsException, ComotException;

}
