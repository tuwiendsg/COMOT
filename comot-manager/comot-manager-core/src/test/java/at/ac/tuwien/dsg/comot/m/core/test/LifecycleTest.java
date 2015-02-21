package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.m.common.Action;
import at.ac.tuwien.dsg.comot.m.common.State;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;

public class LifecycleTest extends AbstractTest {

	@Test
	public void testCycle() throws IOException, JAXBException {

		StateMessage msg = new StateMessage("vvvvvvvvvvvvvvv", Action.CREATE_NEW_INSTANCE);
		msg.addOne("ooo", State.NONE, State.IDLE);

		String message = Utils.asJsonString(msg);

		log.info(message);
		// CloudService service = STemplates.fullService();

		// LifeCycleManager cycle = new LifeCycleManager();
		// cycle.setUp();
		// cycle.send("aaa.bbb", "this is message");
		// cycle.cleanUp();
	}

}
