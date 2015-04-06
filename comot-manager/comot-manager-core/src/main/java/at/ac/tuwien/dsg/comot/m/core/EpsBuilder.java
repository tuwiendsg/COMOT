package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.EpsAdapterStatic;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;

@Component
public class EpsBuilder extends Processor {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected AmqpAdmin admin;

	// protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());
	//
	// enum DynamicEpsState {
	// CREATED_SENT, ASSIGNMENT_REQUESTED
	// }

	@Override
	public void start() throws Exception {

		admin.declareExchange(new TopicExchange(Constants.EXCHANGE_DYNAMIC_REGISTRATION, false, false));

		infoService.deeteAll();
		context.getBean(InitData.class).setUpTestData();

		// create static EPSes
		for (OfferedServiceUnit osu : infoService.getOsus()) {
			try {

				if (osu.getService() == null) {

					Class<?> clazz = null;
					String ip = null;
					String port = null;

					for (Resource res : osu.getResources()) {
						if (res.getType().getName().equals(Constants.ADAPTER_CLASS)) {
							clazz = Class.forName(res.getName());

						} else if (res.getType().getName().equals(Constants.IP)) {
							ip = res.getName();

						} else if (res.getType().getName().equals(Constants.PORT)) {
							port = res.getName();
						}
					}

					String epsId = infoService.createOsuInstance(osu.getId());
					EpsAdapterStatic adapter = (EpsAdapterStatic) context.getBean(clazz);
					adapter.start(epsId, ip, new Integer(port));

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {
		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingCustom(queueName, "*.*." + EpsEvent.EPS_DYNAMIC_REQUESTED + ".SERVICE"));
		bindings.add(bindingCustom(queueName, "*.*." + EpsEvent.EPS_DYNAMIC_REMOVED + ".SERVICE"));
		bindings.add(bindingCustom(queueName, "*.*." + EpsEvent.EPS_SUPPORT_ASSIGNED + ".SERVICE"));
		bindings.add(bindingLifeCycle(queueName, "*.*.*.*." + Action.CREATED + ".SERVICE.#"));
		// bindings.add(bindingLifeCycle(queueName, "*.*.*.*." + Action.REMOVED + ".SERVICE.#"));
		bindings.add(new Binding(queueName, DestinationType.QUEUE, Constants.EXCHANGE_DYNAMIC_REGISTRATION,
				"#", null));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws ClassNotFoundException, IOException, AmqpException, JAXBException, EpsException {

		if (infoService.isServiceOfDynamicEps(serviceId)) {

			if (action == Action.CREATED) {
				String staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);

				manager.sendCustom(Type.SERVICE, new CustomEvent(serviceId, instanceId, serviceId,
						EpsEvent.EPS_SUPPORT_REQUESTED.toString(), staticDeplId, null));

			}

			// if (action == Action.REMOVED) {
			//
			// String osuId = infoService.getOsuByServiceId(serviceId).getId();
			//
			// for (OsuInstance osuIns : infoService.getOsuInstancesForOsu(osuId)) {
			// if (osuIns.getServiceInstance() == null) {
			// infoService.removeOsuInatance(osuIns.getId());
			// break;
			// }
			// }
			// }
		}
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			String event, String epsId, String origin, String optionalMessage) throws ClassNotFoundException,
			AmqpException, JAXBException, EpsException {

		EpsEvent action = EpsEvent.valueOf(event);

		if (action == EpsEvent.EPS_DYNAMIC_REQUESTED) {

			// // create
			// serviceId = infoService.getOsu(epsId).getService().getId();
			// instanceId = infoService.createServiceInstance(serviceId);
			//
			// manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId,
			// Action.CREATED));

			// // assign
			// String staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
			//
			// manager.sendCustom(Type.SERVICE, new CustomEvent(serviceId, instanceId, serviceId,
			// EpsAction.EPS_SUPPORT_REQUESTED.toString(), staticDeplId, null));

		} else if (action == EpsEvent.EPS_SUPPORT_ASSIGNED && infoService.isServiceOfDynamicEps(serviceId)) {

			manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.START));

		} else if (action == EpsEvent.EPS_DYNAMIC_REMOVED) {

			manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.REMOVED));

		}

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
