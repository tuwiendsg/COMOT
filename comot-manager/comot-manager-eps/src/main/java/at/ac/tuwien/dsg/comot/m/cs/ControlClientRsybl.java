/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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

import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlEventsListener;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Constraint;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Strategy;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionPlanEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientRsybl implements ControlClient {

	private static final Logger log = LoggerFactory.getLogger(ControlClientRsybl.class);

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
		rsybl.updateServiceDescription(service.getId(), UtilsCs.asString(cloudServiceXML));
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

							if (event instanceof ActionPlanEvent) {
								ActionPlanEvent apEvent = (ActionPlanEvent) event;

								log.info(
										"ALL onActionPlanEvent(serviceId={}, stage={}, type={}, strategies={}, constraints={}, effects={})",
										apEvent.getServiceId(), apEvent.getStage(), apEvent.getType(),
										extractStrategies(apEvent.getStrategies()),
										extractConstraints(apEvent.getConstraints()), apEvent.getEffect());

							} else if (event instanceof ActionEvent) {
								ActionEvent aEvent = (ActionEvent) event;

								log.info(
										"ALL onActionEvent(serviceId={}, stage={}, type={}, actionId={}, targetId={})",
										aEvent.getServiceId(), aEvent.getStage(), aEvent.getType(),
										aEvent.getActionId(), aEvent.getTargetId());

							} else if (event instanceof at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent) {
								at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent aEvent = (at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent) event;

								log.info(
										"ALL onCustomEvent(serviceId={}, stage={}, type={}, targetId={}, message={})",
										aEvent.getServiceId(), aEvent.getStage(), aEvent.getType(), aEvent.getTarget(),
										aEvent.getMessage());
							} else {
								log.warn("ALL unexpected IEvent serviceId={} stage={} {}", event.getServiceId(),
										event.getStage(), obj);
							}

							// log.debug("IEvent serviceId={} stage={} {}", event.getServiceId(), event.getStage(),
							// obj);

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

	public static String extractStrategies(List<Strategy> list) {

		StringBuilder builder = new StringBuilder("[");

		for (Strategy one : list) {
			if (builder.length() > 1) {
				builder.append(", ");
			}
			builder.append(one.getId() + " " + one.getCondition() + " " + one.getToEnforce());
		}
		builder.append("]");
		return builder.toString();
	}

	public static String extractConstraints(List<Constraint> list) {

		StringBuilder builder = new StringBuilder("[");

		for (Constraint one : list) {
			if (builder.length() > 1) {
				builder.append(", ");
			}
			if (one == null) {
				builder.append("null");
			} else {
				builder.append(one.getId() + " " + one.getCondition() + " " + one.getToEnforce());
			}
		}
		builder.append("]");
		return builder.toString();
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
