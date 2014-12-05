package at.ac.tuwien.dsg.comot.common.coreservices;

import java.util.List;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface MonitoringClient extends CoreServiceClient {

	// start
	public void startMonitoring(
			CloudService sevice,
			CompositionRulesConfiguration mcr) throws CoreServiceException, ComotException;

	// stop
	public void stopMonitoring(
			String serviceId) throws CoreServiceException;

	// update
	public void updateService(
			String serviceId,
			CloudService sevice) throws CoreServiceException, ComotException;

	public void setMcr(
			String serviceId,
			CompositionRulesConfiguration mcr) throws CoreServiceException;

	// get
	public ElementMonitoring getMonitoringData(
			String serviceId) throws CoreServiceException, ComotException;

	public CompositionRulesConfiguration getMcr(
			String serviceId) throws CoreServiceException;

	public List<String> listAllServices() throws CoreServiceException;

	// public void getServiceDescription(String serviceId) throws CoreServiceException;
}
