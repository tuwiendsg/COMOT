package at.ac.tuwien.dsg.comot.m.common.coreservices;

import java.util.List;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface MonitoringClient extends ElasticPlatformServiceClient {

	// start
	public void startMonitoring(
			CloudService sevice) throws EpsException, ComotException;

	// stop
	public void stopMonitoring(
			String serviceId) throws EpsException;

	// update
	public void updateService(
			String serviceId,
			CloudService sevice) throws EpsException, ComotException;

	public void setMcr(
			String serviceId,
			CompositionRulesConfiguration mcr) throws EpsException;

	// get
	public ElementMonitoring getMonitoringData(
			String serviceId) throws EpsException, ComotException;

	public CompositionRulesConfiguration getMcr(
			String serviceId) throws EpsException;

	public List<String> listAllServices() throws EpsException;

	boolean isMonitored(String instanceId) throws EpsException;

	// public void getServiceDescription(String serviceId) throws CoreServiceException;
}
