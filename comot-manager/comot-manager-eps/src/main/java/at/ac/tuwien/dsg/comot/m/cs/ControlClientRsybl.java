package at.ac.tuwien.dsg.comot.m.cs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlEventsListener;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientRsybl implements ControlClient {

	private final Logger log = LoggerFactory.getLogger(ControlClientRsybl.class);

	protected RsyblClient rsybl;
	@Autowired
	protected RsyblMapper rsyblMapper;
	@Autowired
	protected DeploymentMapper deploymentMapper;

	protected String QUEUE_NAME = "events";
	protected ConnectionFactory factory;
	protected Connection connection;
	protected Session session;
	protected MessageConsumer consumer;
	protected boolean first = true;

	protected Map<String, ControlEventsListener> listanersMap = Collections
			.synchronizedMap(new HashMap<String, ControlEventsListener>());

	public ControlClientRsybl(RsyblClient rsybl) {
		super();
		this.rsybl = rsybl;
	}

	@Override
	public void sendInitialConfig(
			CloudService service) throws EpsException, JAXBException {

		if (service == null) {
			log.warn("sendInitialConfig(service=null )");
			return;
		}

		String serviceId = service.getId();
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		DeploymentDescription deploymentDescription = deploymentMapper.extractDeployment(service);

		rsybl.prepareControl(serviceId);

		rsybl.serviceDescription(serviceId, UtilsCs.asString(cloudServiceXML));

		rsybl.serviceDeployment(serviceId, deploymentDescription);

	}

	@Override
	public void createMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException {
		rsybl.sendMetricsCompositionRules(serviceId, compositionRulesConfiguration);
	}

	@Override
	public void createEffects(String serviceId, String effectsJSON) throws EpsException {
		rsybl.sendElasticityCapabilitiesEffects(serviceId, effectsJSON);
	}

	@Override
	public void updateService(CloudService service) throws EpsException, JAXBException {
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		rsybl.updateElasticityRequirements(service.getId(), UtilsCs.asString(cloudServiceXML));
	}

	@Override
	public void updateMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException {
		rsybl.updateMetricsCompositionRules(serviceId, compositionRulesConfiguration);
	}

	@Override
	public void updateEffects(String serviceId, String effectsJSON) throws EpsException {
		rsybl.updateElasticityCapabilitiesEffects(serviceId, effectsJSON);
	}

	@Override
	public void startControl(String serviceId) throws EpsException {
		rsybl.startControl(serviceId);
	}

	@Override
	public void stopControl(String serviceId) throws EpsException {
		rsybl.stopControl(serviceId);
	}

	@Override
	public void setHostAndPort(String host, int port) {
		rsybl.setBaseUri(UriBuilder.fromUri(rsybl.getBaseUri())
				.host(host).port(port).build());
	}

	@Override
	public List<String> listAllServices() throws EpsException {
		return rsybl.listAllServices();
	}

	@Override
	public void removeService(String serviceId) throws EpsException {
		rsybl.removeService(serviceId);

	}

	@Override
	public boolean isControlled(String serviceId) throws EpsException {

		for (String id : rsybl.listAllServices()) {
			if (id.equals(serviceId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void registerForEvents(String serviceId, ControlEventsListener listener) throws JMSException {

		if (first) {
			first = false;

			factory = new ActiveMQConnectionFactory("tcp://" + rsybl.getHost() + ":61616");
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(session.createQueue(QUEUE_NAME));

			MessageListener jmsListener = new MessageListener() {

				@Override
				public void onMessage(Message message) {

					try {
						Object obj = ((ObjectMessage) message).getObject();

						if (obj instanceof IEvent) {
							IEvent event = (IEvent) obj;
							log.debug("IEvent serviceId={} stage={} {}", event.getServiceId(), event.getStage(), obj);

							if (listanersMap.containsKey(event.getServiceId())) {
								listanersMap.get(event.getServiceId()).onMessage(event);
							}

						} else {
							log.warn("unexpected JMS message {}", obj);
						}

					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			};

			consumer.setMessageListener(jmsListener);
			connection.start();
		}

		listanersMap.put(serviceId, listener);
	}

	@Override
	public void removeListener(String serviceId) {
		listanersMap.remove(serviceId);
	}

	@PreDestroy
	public void cleanup() throws JMSException {
		if (consumer != null) {
			consumer.close();
		}
		if (session != null) {
			session.close();
		}
		if (connection != null) {
			connection.close();
		}

		if (rsybl != null) {
			log.info("closing rsybl client");
			rsybl.close();
		}
	}
}
