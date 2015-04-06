package at.ac.tuwien.dsg.comot.m.core.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Deployment;
import at.ac.tuwien.dsg.comot.m.common.EpsAdapterStatic;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;

@Component
@Scope("prototype")
public class DeploymentAdapterStatic implements EpsAdapterStatic {

	@Autowired
	protected Deployment processor;
	@Autowired
	protected PerInstanceQueueManager manager;
	@Autowired
	protected InformationClient infoService;

	public void start(String participantId, String host, int port) throws Exception {

		processor.setHostAndPort(host, port);

		manager.start(participantId, processor);
	}

}
