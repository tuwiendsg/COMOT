package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Quality;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class EpsCoordinator {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String QUEUE_PREPARATION = "QUEUE_PREPARATION";

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

	@PostConstruct
	public void setUp() {

		admin.declareQueue(new Queue(QUEUE_PREPARATION, false, false, false));
		admin.declareBinding(new Binding(
				QUEUE_PREPARATION, DestinationType.QUEUE,
				AppContextCore.EXCHANGE_SERVICES,
				Action.NEW_INSTANCE_REQUESTED + ".#", null));

		for (OfferedServiceUnit osu : infoService.getOsus().values()) {
			try {
				for (Resource res : osu.getResources()) {
					if (res.getType().getName().equals(InformationServiceMock.TYPE_STATIC_SERVICE)) {
						for (Resource res2 : res.getContainsResources()) {
							if (res2.getType().getName().equals(InformationServiceMock.ADAPTER_CLASS)) {

								Adapter adapter = (Adapter) context.getBean(Class.forName(res2.getName()));
								adapter.start(osu.getId(), null, null);

							}
						}

					}
				}
			} catch (BeansException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	@RabbitListener(queues = QUEUE_PREPARATION)
	public void prepareServiceInstance(String data) {

		try {

			log.info("wwwwwwwwwwww {}", data);

			StateMessage message = Utils.asStateMessage(data);
			String instanceId = message.getEvent().getCsInstanceId();
			String serviceId = message.getEvent().getServiceId();
			for (OfferedServiceUnit osu : infoService.getSupportingServices(instanceId)) {

				for (Quality quality : osu.getQualities()) {
					if (quality.getType().getName().equals(InformationServiceMock.TYPE_ACTION)
							&& quality.getName().equals(Action.NEW_INSTANCE_REQUESTED)) {

						// TODO instantiating new EPS
						log.warn("instantiating new EPS");
					}
				}
			}

			EventMessage event = new EventMessage(serviceId, instanceId, serviceId, Action.PREPARED, null);
			lcManager.executeAction(event);

		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

	}

}
