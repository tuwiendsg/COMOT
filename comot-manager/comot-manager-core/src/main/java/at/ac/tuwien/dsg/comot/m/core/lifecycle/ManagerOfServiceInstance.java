package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

@Component("prototype")
public class ManagerOfServiceInstance {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected String serviceId;
	protected String csInstanceId;
	
	
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager manager;
	
	protected Group serviceGroup;
	protected Map<String,Group> groups= new HashMap<>();
	
	
	
	public void createNewInstance(String serviceId, String csInstanceId) throws JAXBException, IOException{
		
		this.serviceId=serviceId;
		this.csInstanceId=csInstanceId;
		
		CloudService service =infoService.getServiceInformation(csInstanceId);
		
		serviceGroup = new Group(service, new AggregationStrategy());
		groups.put(csInstanceId, serviceGroup);
		log.info(" {}", serviceGroup);
		
		String message = Utils.asJsonString(new StateMessage(csInstanceId, serviceGroup.getCurrentState()));
		
		log.info(message);
		
		manager.send(serviceId+"."+csInstanceId+".#", message);
		
	}
	
	

	
	

}
