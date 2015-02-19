package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.m.common.State;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class LifecycleTest extends AbstractTest {

	@Test
	public void testCycle() throws IOException, JAXBException {

		StateMessage msg = new StateMessage("vvvvvvvvvvvvvvv", State.DEPLOYMENT);
		msg.addOne("other",  State.DEPLOYMENT);
		
		String message = Utils.asJsonString(msg);

		log.info(message);
		// CloudService service = STemplates.fullService();

		// LifeCycleManager cycle = new LifeCycleManager();
		// cycle.setUp();
		// cycle.send("aaa.bbb", "this is message");
		// cycle.cleanUp();
	}

}
