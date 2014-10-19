package at.ac.tuwien.dsg.comot.client.test;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.core.test.samples.ExampleDeployOneVM;

public class ControlClientTest extends AbstractTest{

	@Test
	public void aaa() throws CoreServiceException{		
		deployment.deploy(ExampleDeployOneVM.build());
	}
}
