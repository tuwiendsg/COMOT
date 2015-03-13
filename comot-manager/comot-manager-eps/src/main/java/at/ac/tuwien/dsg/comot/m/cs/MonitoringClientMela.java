package at.ac.tuwien.dsg.comot.m.cs;

import java.io.IOException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.m.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.MelaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.MelaOutputMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class MonitoringClientMela implements MonitoringClient {

	private final Logger log = LoggerFactory.getLogger(MonitoringClientMela.class);

	@Autowired
	protected MelaClient mela;
	@Autowired
	protected MelaMapper melaMapper;
	@Autowired
	protected MelaOutputMapper mapperMelaOutput;

	@Override
	public void startMonitoring(CloudService service) throws CoreServiceException,
			ComotException {

		if (service == null) {
			log.warn("startMonitoring(service=null )");
			return;
		}

		try {

			MonitoredElement element = melaMapper.extractMela(service);
			Requirements requirements = melaMapper.extractRequirements(service);

			try {
				log.info("MonitoredElement: {}", UtilsCs.asString(element));
			} catch (JAXBException e) {
				e.printStackTrace();
			}
			
			mela.sendServiceDescription(element);

			if (requirements != null && !requirements.getRequirements().isEmpty()) {
				mela.sendRequirements(service.getId(), requirements);
			}

		} catch (ClassNotFoundException | IOException e) {
			throw new ComotException("Mapping from Model to Mela failed ", e);
		}
	}

	@Override
	public void stopMonitoring(String serviceId) throws CoreServiceException {
		mela.removeServiceDescription(serviceId);

	}

	@Override
	public void updateService(String serviceId, CloudService sevice) throws CoreServiceException, ComotException {

		MonitoredElement element;
		try {
			element = melaMapper.extractMela(sevice);
		} catch (ClassNotFoundException | IOException e) {
			throw new ComotException("Mapping from CloudService to MonitoredElement failed ", e);
		}

		mela.updateServiceDescription(serviceId, element);
	}

	@Override
	public void setMcr(String serviceId, CompositionRulesConfiguration mcr) throws CoreServiceException {
		mela.sendMetricsCompositionRules(serviceId, mcr);
	}

	@Override
	public CompositionRulesConfiguration getMcr(String serviceId) throws CoreServiceException {
		CompositionRulesConfiguration mcr = mela.getMetricsCompositionRules(serviceId);
		return mcr;
	}

	@Override
	public ElementMonitoring getMonitoringData(String serviceId) throws CoreServiceException, ComotException {

		MonitoredElementMonitoringSnapshot snapshot = mela.getMonitoringData(serviceId);
		try {
			return mapperMelaOutput.extractOutput(snapshot);
		} catch (JAXBException e) {
			throw new ComotException("Mapping from MonitoredElementMonitoringSnapshot to ElementMonitoring failed ", e);
		}
	}

	@Override
	public List<String> listAllServices() throws CoreServiceException {
		return mela.listAllServices();

	}

	// @Override
	// public void getServiceDescription(String serviceId) throws CoreServiceException {
	// // TODO Auto-generated method stub
	//
	// }

	@PreDestroy
	public void cleanup() {
		log.info("closing mela client");
		mela.close();
	}

	@Override
	public void setHost(String host) {
		mela.setHost(host);
	}

	@Override
	public void setPort(int port) {
		mela.setPort(port);
	}

}
