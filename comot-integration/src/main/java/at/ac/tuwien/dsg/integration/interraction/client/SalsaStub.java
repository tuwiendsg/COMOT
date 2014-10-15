package at.ac.tuwien.dsg.integration.interraction.client;

import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaStub {

	public void deploy(CloudService CloudService) {

	}

	public void undeploy(String serviceId) {

	}

	public void spawn(String serviceId, String topologyId, String nodeId, int instanceCount) {

	}

	public void destroy(String serviceId, String topologyId, String nodeId, String instanceId) {

	}

	public CloudService getStatus(String serviceId) {
		return null;

	}

	public DeploymentDescription getServiceDeploymentInfo(String serviceId) {
		return null;

	}

}
