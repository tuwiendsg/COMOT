package at.ac.tuwien.dsg.comot.cs;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.cs.transformer.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.cs.transformer.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class DeploymentClientSalsa implements DeploymentClient {

	private final Logger log = LoggerFactory.getLogger(DeploymentClientSalsa.class);

	protected SalsaClient salsa;
	// @Autowired
	protected ToscaDescriptionBuilder toscaBuilder;

	public DeploymentClientSalsa() {
		
		toscaBuilder = new ToscaDescriptionBuilderImpl();
		// toscaBuilder.setValidating(true); // TODO validation fails
		
		salsa = new SalsaClient();
	}

	@Override
	public String deploy(CloudService CloudService) throws CoreServiceException {

		String toscaDescriptionXml = toscaBuilder.toXml(CloudService);

		return salsa.deploy(toscaDescriptionXml);
	}
	
	@Override
	public String deploy(String toscaDescriptionXml) throws CoreServiceException {
		return salsa.deploy(toscaDescriptionXml);
	}

	@Override
	public void undeploy(String serviceId) throws CoreServiceException {
		salsa.undeploy(serviceId);
	}

	@Override
	public void spawn(String serviceId, String topologyId, String nodeId, int instanceCount)
			throws CoreServiceException {
		salsa.spawn(serviceId, topologyId, nodeId, instanceCount);
	}

	@Override
	public void destroy(String serviceId, String topologyId, String nodeId, int instanceId) throws CoreServiceException {
		salsa.destroy(serviceId, topologyId, nodeId, instanceId);
	}

	@Override
	public at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService getStatus(String serviceId)
			throws CoreServiceException {
		return salsa.getStatus(serviceId);
	}

	@Override
	public DeploymentDescription getServiceDeploymentInfo(String serviceId) throws CoreServiceException {
		return salsa.getServiceDeploymentInfo(serviceId);
	}

	@Override
	public boolean isRunning(String serviceID) throws CoreServiceException {

		boolean running = false;

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service = salsa.getStatus(serviceID);

		log.info("Service {} is in state={}", serviceID, service.getState());

		if (service.getState().equals(SalsaEntityState.DEPLOYED)
				|| service.getState().equals(SalsaEntityState.RUNNING)) {
			running = true;

			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit serviceUnit : service
					.getAllComponentByType(SalsaEntityType.SOFTWARE)) {

				if (serviceUnit != null
						&& !(serviceUnit.getState().equals(SalsaEntityState.DEPLOYED) || serviceUnit.getState()
								.equals(SalsaEntityState.RUNNING))) {
					running = false;
					break;
				}
			}
		}

		return running;
	}

	@PreDestroy
	public void cleanup() {
		log.info("closing salsa client");
		salsa.close();
	}

	@Override
	public void setHost(String host) {
		salsa.setHost(host);
	}

	@Override
	public void setPort(int port) {
		salsa.setPort(port);
	}

}
