package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.UtilsLc;
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
public class DeploymentAdapter extends Adapter {

	@Autowired
	protected DeploymentClient deployment;

	protected Binding binding1;
	protected Binding binding2;

	@Override
	public void start(String osuInstanceId) {

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

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.IDLE + ".#", null);
		binding2 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE.*." + State.UNDEPLOYMENT + ".#", null);

		admin.declareBinding(binding1);
		admin.declareBinding(binding2);

		container.setMessageListener(new CustomListener());

	}

	class CustomListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			try {

				StateMessage msg = stateMessage(message);
				String instanceId = msg.getEvent().getCsInstanceId();
				String serviceId = msg.getEvent().getServiceId();
				Action action = msg.getEvent().getAction();

				log.info("onMessage {}", Utils.asJsonString(msg));

				if (isAssignedTo(instanceId)) {

					if (action.equals(Action.PREPARED)) {

						CloudService service = infoService.getServiceInstance(instanceId);
						service.setId(instanceId);
						service.setName(instanceId);

						service = deployment.deploy(service);

						monitorStatusUntilDeployed(serviceId, service);

					} else if (action.equals(Action.UNDEPLOYMENT_REQUESTED)) { // TODO this will probably be different
																				// action

					}
				}

			} catch (JAXBException | CoreServiceException | ComotException | IOException | InterruptedException
					| ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	protected void monitorStatusUntilDeployed(String serviceId, CloudService service) throws CoreServiceException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		Map<String, State> map;
		State oldState;

		service = UtilsLc.removeProviderInfo(service);

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
					Action action = UtilsLc.translateToAction(oldState, instance.getState());

					if (action == null) {
						log.error("invalid transitions {} -> {}", oldState, instance.getState());
					} else {
						lcManager.executeAction(new EventMessage(serviceId, service.getId(), instance.getId(), action,
								service, null));
					}
				}
			}

		} while (!service.getState().equals(State.OPERATION_RUNNING));

		log.info("stopped checking");

	}

	@Override
	protected void clean() {
		if (binding1 != null) {
			admin.removeBinding(binding1);
		}
		if (binding2 != null) {
			admin.removeBinding(binding2);
		}
	}

}
