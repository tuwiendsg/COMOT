package at.ac.tuwien.dsg.comot.m.adapter.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.common.EpsAdapter;

@Component
@Scope("prototype")
public class ControlAdapter implements EpsAdapter {

	@Autowired
	protected Control processor;
	@Autowired
	protected PerInstanceQueueManager manager;

	public void start(String participantId) throws Exception {
		manager.start(participantId, processor);
	}

}
