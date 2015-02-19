package at.ac.tuwien.dsg.comot.m.core.test;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class LifecycleTest extends AbstractTest { 
	
	@Test
	public void testCysle(){
		
		
		CloudService service = STemplates.fullService();
		
		LifeCycleManager cycle = new LifeCycleManager();
		cycle.setUpLifecycle(service);
	}

}
