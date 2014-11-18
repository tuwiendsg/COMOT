package at.ac.tuwien.dsg.comot.common.coreservices;

import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;

public interface MonitoringClient extends CoreServiceClient {

	// start
	public void startMonitoring(
			CloudService sevice,
			CompositionRulesConfiguration mcr) throws CoreServiceException;

	// stop
	public void stopMonitoring(
			String serviceId) throws CoreServiceException;

	// update
	public void updateService(
			String serviceId,
			CloudService sevice) throws CoreServiceException;

	public void updateMcr(
			String serviceId,
			CompositionRulesConfiguration mcr) throws CoreServiceException;

	// get
	public MonitoredElementMonitoringSnapshot getMonitoringData(
			String serviceId) throws CoreServiceException;

	public CompositionRulesConfiguration getMetricsCompositionRules(
			String serviceId) throws CoreServiceException;

	// public void getServiceDescription(String serviceId) throws CoreServiceException;
}
