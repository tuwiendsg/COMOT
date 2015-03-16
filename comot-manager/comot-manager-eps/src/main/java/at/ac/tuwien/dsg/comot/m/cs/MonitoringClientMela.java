package at.ac.tuwien.dsg.comot.m.cs;

import java.io.IOException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
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

	protected MelaClient mela;
	@Autowired
	protected MelaMapper melaMapper;
	@Autowired
	protected MelaOutputMapper mapperMelaOutput;

	public MonitoringClientMela(MelaClient mela) {
		super();
		this.mela = mela;
	}

	@Override
	public void startMonitoring(CloudService service) throws EpsException,
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
	public void stopMonitoring(String serviceId) throws EpsException {
		mela.removeServiceDescription(serviceId);

	}

	@Override
	public void updateService(String serviceId, CloudService sevice) throws EpsException, ComotException {

		MonitoredElement element;
		try {
			element = melaMapper.extractMela(sevice);
		} catch (ClassNotFoundException | IOException e) {
			throw new ComotException("Mapping from CloudService to MonitoredElement failed ", e);
		}

		mela.updateServiceDescription(serviceId, element);
	}

	@Override
	public void setMcr(String serviceId, CompositionRulesConfiguration mcr) throws EpsException {
		mela.sendMetricsCompositionRules(serviceId, mcr);
	}

	@Override
	public CompositionRulesConfiguration getMcr(String serviceId) throws EpsException {
		CompositionRulesConfiguration mcr = mela.getMetricsCompositionRules(serviceId);
		return mcr;
	}

	@Override
	public ElementMonitoring getMonitoringData(String serviceId) throws EpsException, ComotException {

		MonitoredElementMonitoringSnapshot snapshot = mela.getMonitoringData(serviceId);
		try {
			return mapperMelaOutput.extractOutput(snapshot);
		} catch (JAXBException e) {
			throw new ComotException("Mapping from MonitoredElementMonitoringSnapshot to ElementMonitoring failed ", e);
		}
	}

	@Override
	public List<String> listAllServices() throws EpsException {
		return mela.listAllServices();

	}

	public boolean isMonitored(String instanceId) throws EpsException {

		for (String id : mela.listAllServices()) {
			if (id.equals(instanceId)) {
				return true;
			}
		}
		return false;
	}

	// @Override
	// public void getServiceDescription(String serviceId) throws CoreServiceException {
	// // TODO Auto-generated method stub
	//
	// }

	@PreDestroy
	public void cleanup() {
		if (mela != null) {
			log.info("closing mela client");
			mela.close();
		}
	}

	@Override
	public void setHostAndPort(String host, int port) {
		mela.setBaseUri(UriBuilder.fromUri(mela.getBaseUri())
				.host(host).port(port).build());
	}

}
