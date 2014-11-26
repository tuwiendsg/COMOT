package at.ac.tuwien.dsg.comot.ui.test;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class RestInterfaceTest extends AbstractTest {

	

	
	@Test
	public void testMonitoring() throws CoreServiceException, ComotException{
		
		//put("http://localhost:8380/comot/rest/ElasticIoTPlatform/monitoring");
		
		orchestrator.startMonitoring("ElasticIoTPlatform");
		
	}
}
