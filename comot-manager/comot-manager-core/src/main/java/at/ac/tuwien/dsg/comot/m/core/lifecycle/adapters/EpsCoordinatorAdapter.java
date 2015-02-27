package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Quality;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class EpsCoordinatorAdapter extends Adapter {

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ToscaMapper mapperTosca;

	protected Binding binding1;
	protected Binding binding2;

	@PostConstruct
	public void setUp() {
		startAdapter("EPS_COORDINATOR");

		for (OfferedServiceUnit osu : infoService.getOsus().values()) {
			try {
				for (Resource res : osu.getResources()) {
					if (res.getType().getName().equals(InformationServiceMock.TYPE_STATIC_SERVICE)) {
						for (Resource res2 : res.getContainsResources()) {
							if (res2.getType().getName().equals(InformationServiceMock.ADAPTER_CLASS)) {

								Adapter adapter = (Adapter) context.getBean(Class.forName(res2.getName()));
								adapter.startAdapter(osu.getId());

							}
						}

					}
				}
			} catch (BeansException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void start(String osuInstanceId) {

		binding1 = new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				"*.TRUE." + State.NONE + "." + State.PREPARATION + ".#", null);

		admin.declareBinding(binding1);
		container.setMessageListener(new CustomListener());

	}

	class CustomListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			try {

				StateMessage msg = stateMessage(message);
				String csInstanceId = msg.getCsInstanceId();
				String serviceId = msg.getServiceId();

				for (OfferedServiceUnit osu : infoService.getSupportingServices(csInstanceId)) {

					for (Quality quality : osu.getQualities()) {
						if (quality.getType().getName().equals(InformationServiceMock.TYPE_ACTION)
								&& quality.getName().equals(Action.NEW_INSTANCE_REQUESTED)) {

							// TODO instantiating new EPS
							log.warn("instantiating new EPS");
						}
					}
				}

				EventMessage event = new EventMessage(serviceId, csInstanceId, serviceId, Action.PREPARED, msg
						.getEvent().getService(), null);
				lcManager.executeAction(event);

			} catch (IOException | JAXBException | IllegalArgumentException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void clean() {
		if (binding1 != null) {
			admin.removeBinding(binding1);
		}
	}

}
