package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected Group serviceGroup;
	protected Map<String,Group> groups= new HashMap<>();
	
	public void setUpLifecycle(CloudService service){
		
		serviceGroup = new Group(service, new AggregationStrategy());
		
		
		log.info(" {}", serviceGroup);
	}
	
}
