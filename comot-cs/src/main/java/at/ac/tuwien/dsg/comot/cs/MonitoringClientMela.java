package at.ac.tuwien.dsg.comot.cs;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.cs.mapper.MelaMapper;
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

	

	@Override
	public void startMonitoring(CloudService sevice, CompositionRulesConfiguration mcr) throws CoreServiceException {

		MonitoredElement element = melaMapper.extractMela(sevice);
		Requirements requirements = melaMapper.extractRequirements(sevice);

		mela.sendServiceDescription(element);
		//TODO check not to send reqs when it has not sense
		mela.sendRequirements(sevice.getId(), requirements);
		//TODO check not to send MCR when it has not sense
		mela.sendMetricsCompositionRules(sevice.getId(), mcr);
	}

	@Override
	public void stopMonitoring(String serviceId) throws CoreServiceException {
		mela.removeServiceDescription(serviceId);

	}

	@Override
	public void updateService(String serviceId, CloudService sevice) throws CoreServiceException {

		MonitoredElement element = melaMapper.extractMela(sevice);

		mela.updateServiceDescription(serviceId, element);
	}

	@Override
	public void updateMcr(String serviceId, CompositionRulesConfiguration mcr) throws CoreServiceException {
		mela.sendMetricsCompositionRules(serviceId, mcr);
	}

	@Override
	public MonitoredElementMonitoringSnapshot getMonitoringData(String serviceId) throws CoreServiceException {
		
		MonitoredElementMonitoringSnapshot snapshot = mela.getMonitoringData(serviceId);
		return snapshot;

	}

	@Override
	public CompositionRulesConfiguration getMetricsCompositionRules(String serviceId) throws CoreServiceException {
		
		CompositionRulesConfiguration mcr = mela.getMetricsCompositionRules(serviceId);
		return mcr;
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
