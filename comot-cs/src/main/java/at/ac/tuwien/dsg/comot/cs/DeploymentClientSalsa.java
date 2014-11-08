package at.ac.tuwien.dsg.comot.cs;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.UtilsMapper;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class DeploymentClientSalsa implements DeploymentClient {

	private final Logger log = LoggerFactory.getLogger(DeploymentClientSalsa.class);

	protected SalsaClient salsa;

	@Autowired
	protected ToscaMapper mapper;

	public DeploymentClientSalsa() {
		salsa = new SalsaClient();
	}

	@Override
	public String deploy(CloudService cloudService) throws CoreServiceException, ComotException {

		String toscaDescriptionXml;

		try {
			toscaDescriptionXml = UtilsMapper.asString(mapper.toTosca(cloudService));
		} catch (JAXBException e) {
			throw new ComotException("Failed to marshall TOSCA into XML ", e);
		}

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

	// TODO only temporary
	@Override
	public String getStatusGui(String serviceId) throws CoreServiceException {
		return salsa.getStatusGui(serviceId);
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
