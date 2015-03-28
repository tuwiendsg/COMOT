package at.ac.tuwien.dsg.comot.m.core.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.EpsAdapterStatic;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.InitData;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class EpsBuilder extends Processor {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InformationClient infoService;

	@Autowired
	protected AmqpAdmin admin;

	// protected Map<String, DynamicEpsState> managedSet = Collections
	// .synchronizedMap(new HashMap<String, DynamicEpsState>());

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());

	enum DynamicEpsState {
		CREATED_SENT, ASSIGNMENT_REQUESTED
	}

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

		bindings.add(bindingCustom(queueName, "*.*." + EpsAction.EPS_DYNAMIC_REQUESTED + ".SERVICE"));
		bindings.add(bindingCustom(queueName, "*.*." + EpsAction.EPS_DYNAMIC_REMOVED + ".SERVICE"));
		bindings.add(bindingCustom(queueName, "*.*." + EpsAction.EPS_ASSIGNED + ".SERVICE"));
		bindings.add(bindingLifeCycle(queueName, "*.*.*.*." + Action.CREATED + ".SERVICE.#"));
		bindings.add(bindingLifeCycle(queueName, "*.*.*.*." + Action.REMOVED + ".SERVICE.#"));
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
						EpsAction.EPS_ASSIGNMENT_REQUESTED.toString(), getId(), staticDeplId, null));

			} else if (action == Action.REMOVED) {

				String osuId = infoService.getOsuByServiceId(serviceId).getId();

				for (OsuInstance osuIns : infoService.getOsuInstancesForOsu(osuId)) {
					if (osuIns.getServiceInstance() == null) {
						infoService.removeOsuInatance(osuIns.getId());
						break;
					}
				}
			}

		}

	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String instanceId, String groupId,
			String event, String epsId, String origin, String optionalMessage) throws ClassNotFoundException,
			AmqpException, JAXBException, EpsException {

		EpsAction action = EpsAction.valueOf(event);

		// if (action == EpsAction.EPS_DYNAMIC_REQUESTED) {
		//
		// createDynamicEps(optionalMessage);
		//
		// } else
		if (action == EpsAction.EPS_DYNAMIC_CREATED) {

			String osuId = optionalMessage;
			String osuInstanceId = origin;

			List<ServiceInstance> freeInstances = infoService.getEpsServiceInstancesWithoutOsuInstance(osuId);

			if (freeInstances.isEmpty()) {
				log.error("No free EpsServiceInstane to assign the created dynamic EPS to.");
			} else {

				serviceId = infoService.getOsu(osuId).getService().getId();
				instanceId = freeInstances.get(0).getId();
				infoService.createOsuInstanceDynamic(osuId, instanceId, osuInstanceId);

				manager.sendCustom(Type.SERVICE,
						new CustomEvent(serviceId, instanceId, serviceId,
								EpsAction.EPS_DYNAMIC_CREATED.toString(),
								getId(), osuInstanceId, null));

			}
			//
			// } else if (action == EpsAction.) {
			//
			//

		} else if (action == EpsAction.EPS_ASSIGNED && infoService.isServiceOfDynamicEps(serviceId)) {

			manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.STARTED,
					getId()));

		}

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String instanceId, String originId,
			Exception e) throws Exception {
		// TODO Auto-generated method stub

	}

	// protected void createDynamicEps(String epsId) throws EpsException, AmqpException, JAXBException {
	//
	// OfferedServiceUnit osu = infoService.getOsu(epsId);
	//
	// if (osu != null && osu.getService() != null) {
	//
	// String serviceId = osu.getService().getId();
	//
	// // create service instance
	// String instanceId = infoService.createServiceInstance(serviceId);
	//
	// manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, instanceId, serviceId, Action.CREATED,
	// getId()));
	//
	// managedSet.add(instanceId);
	//
	// }
	//
	// }

}
