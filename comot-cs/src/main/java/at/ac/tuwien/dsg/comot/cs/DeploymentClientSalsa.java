package at.ac.tuwien.dsg.comot.cs;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;

public class DeploymentClientSalsa implements DeploymentClient {

	private final Logger log = LoggerFactory.getLogger(DeploymentClientSalsa.class);

	@Autowired
	protected SalsaClient salsa;
	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;

	@Override
	public CloudService deploy(CloudService service) throws CoreServiceException, ComotException {

		String toscaDescriptionXml;

		if (service == null) {
			log.warn("deploy(service=null )");
			return null;
		}

		try {
			toscaDescriptionXml = UtilsCs.asString(mapperTosca.extractTosca(service));
		} catch (JAXBException e) {
			throw new ComotException("Failed to marshall TOSCA into XML ", e);
		}

		CloudService deployedService = deploy(toscaDescriptionXml);

		return deployedService;
	}

	@Override
	public CloudService deploy(String service) throws CoreServiceException, ComotException {

		String serviceId = salsa.deploy(service);

		Definitions def = salsa.getTosca(serviceId);
		CloudService deployedService = mapperTosca.createModel(def);

		return deployedService;
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
	public CloudService getService(String serviceId) throws CoreServiceException, ComotException {

		Definitions definitions = salsa.getTosca(serviceId);

		return mapperTosca.createModel(definitions);
	}

	@Override
	public CloudService refreshStatus(CloudService service)
			throws CoreServiceException, ComotException {

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService status = salsa.getStatus(service.getId());

		CloudService copy;
		try {
			copy = (CloudService) Utils.deepCopy(service);
		} catch (ClassNotFoundException | IOException e) {
			throw new ComotException("Failed to deep-copy CloudService id=" + service.getId(), e);
		}

		mapperDepl.enrichModel(copy, status);

		return copy;
	}

	@Override
	public boolean isRunning(String serviceId) throws CoreServiceException, ComotException {

		boolean running = false;

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service = salsa.getStatus(serviceId);

		log.info("Service {} is in state={}", serviceId, service.getState());

		if (service.getState().equals(SalsaEntityState.DEPLOYED)) {
			running = true;

			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit serviceUnit : service
					.getAllComponentByType(SalsaEntityType.SOFTWARE)) {

				if (serviceUnit != null
						&& !(serviceUnit.getState().equals(SalsaEntityState.DEPLOYED))) {
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
