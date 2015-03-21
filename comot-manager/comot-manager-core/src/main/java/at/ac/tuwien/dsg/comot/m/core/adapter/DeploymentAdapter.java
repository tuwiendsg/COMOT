package at.ac.tuwien.dsg.comot.m.core.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.core.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.core.processor.Deployment;

@Component
@Scope("prototype")
public class DeploymentAdapter implements EpsAdapter {

	@Autowired
	protected Deployment processor;
	@Autowired
	protected PerInstanceQueueManager manager;

	public void start(String participantId) {
		manager.start(participantId, processor);
	}

}
