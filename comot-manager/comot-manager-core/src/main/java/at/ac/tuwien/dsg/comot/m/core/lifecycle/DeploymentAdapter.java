package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
// @Scope("prototype") //for some reason this creates multip0le instances
public class DeploymentAdapter implements Adapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String QUEUE = "QUEUE_";

	// protected String osuId;
	protected String osuInstanceId;

	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ConnectionFactory connectionFactory;
	@Autowired
	protected LifeCycleManager lcManager;

	class DeployListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			try {

				log.info("bbbbbbbb {}", new String(message.getBody(), "UTF-8"));

				StateMessage msg = Utils.asStateMessage(new String(message.getBody(), "UTF-8"));

				String instanceId = msg.getEvent().getCsInstanceId();
				String serviceId = msg.getEvent().getServiceId();

				for (OfferedServiceUnit osu : infoService.getSupportingServices(instanceId)) {
					if (osu.getId().equals(osuInstanceId)) {

						log.info("would deploy {}", instanceId);

						CloudService service = infoService.getServiceInformation(serviceId);
						service.setId(instanceId);
						service.setName(instanceId);

						service = deployment.deploy(service);

						monitorStatusUntilDeployed(serviceId, service);

					}
				}

			} catch (JAXBException | CoreServiceException | ComotException | IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	protected void monitorStatusUntilDeployed(String serviceId, CloudService service) throws CoreServiceException,
			ComotException, IOException, JAXBException, InterruptedException {

		Map<String, State> map;
		State oldState;

		do {

			map = new HashMap<>();
			for (ServiceUnit unit : Navigator.getAllUnits(service)) {
				for (UnitInstance instance : unit.getInstances()) {
					map.put(instance.getId(), instance.getState());
				}
			}

			Thread.sleep(1000);

			service = deployment.refreshStatus(service);

			for (ServiceUnit unit : Navigator.getAllUnits(service)) {
				for (UnitInstance instance : unit.getInstances()) {
					if (map.containsKey(instance.getId())) {
						if (map.get(instance.getId()).equals(instance.getState())) {
							continue;
						} else {
							oldState = map.get(instance.getId());
						}
					} else {
						oldState = State.IDLE;
					}

					// publish
					Action action = translateToAction(oldState, instance.getState());

					if (action == null) {
						log.error("invalid transitions {} -> {}", oldState, instance.getState());
					} else {
						lcManager.executeAction(new EventMessage(serviceId, service.getId(), instance.getId(), action,
								Utils.asXmlString(service)));
					}
				}
			}

		} while (!service.getState().equals(State.OPERATION_RUNNING));

		log.info("stopped checking");

	}

	public Action translateToAction(State oldState, State newState) {

		State temp;

		for (Action action : Action.values()) {
			if ((temp = oldState.execute(action)) != null) {
				if (temp.equals(newState)) {
					return action;
				}
			}
		}

		return null;
	}

	@Override
	public void start(String osuInstanceId, String infoIp, String infoPort) {

		this.osuInstanceId = osuInstanceId;

		OfferedServiceUnit osu = infoService.getOsus().get(osuInstanceId);
		String ip = null;
		String port = null;

		for (Resource resource : osu.getResources()) {
			if (resource.getName().equals(InformationServiceMock.PUBLIC_INSTANCE)) {
				for (Resource res : resource.getContainsResources()) {
					if (res.getType().getName().equals(InformationServiceMock.IP)) {
						ip = res.getName();
					} else if (res.getType().getName().equals(InformationServiceMock.PORT)) {
						port = res.getName();
					}
				}
			}
		}

		deployment.setHost(ip);
		deployment.setPort(new Integer(port));

		admin.declareQueue(new Queue(QUEUE + osuInstanceId, false, false, false));
		admin.declareBinding(new Binding(
				QUEUE + osuInstanceId, DestinationType.QUEUE,
				AppContextCore.EXCHANGE_INSTANCE_HIGH_LEVEL,
				"*.*." + State.IDLE + ".#", null));//

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(QUEUE + osuInstanceId);
		container.setMessageListener(new DeployListener());
		container.start();

		log.info("aaaaaaaa");

	}

	@Override
	public void clean() {

	}

}
