package at.ac.tuwien.dsg.elise.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.dsg.elise.Application.repositories.CloudOfferredServiceRepository;
import at.ac.tuwien.dsg.elise.Application.repositories.EntityRepository;
import at.ac.tuwien.dsg.elise.Application.repositories.ServiceUnitRepository;
import at.ac.tuwien.dsg.elise.extension.collectors.OpenStackCollector;

@RestController
@Controller
@Component
@RequestMapping("/elise")
public class EliseServices {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(method=RequestMethod.GET, value="/health")
	public String health(){
		return "good";
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/generate")
	public String generateData(){
		DataGeneration gen = new DataGeneration(enRepo, suRepo, cloudRepo);
		gen.generateAll();
		return "done";
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/updateCloudProvider/DSGOpenStack")
	public String updateDSGOpenstack(){
		
		//logger.debug("dsg" +" - " + prop.getProperty(OpenStackParameterStrings.END_POINT.getString())+" - " + OpenStackParameterStrings.TENANT.getString() +" - " + OpenStackParameterStrings.USERNAME.getString() +" - " + OpenStackParameterStrings.PASSWORD.getString() +" - " + OpenStackParameterStrings.SSH_KEY_NAME.getString());
		OpenStackCollector openstack = new OpenStackCollector("dsg", enRepo, suRepo, cloudRepo);
		openstack.updateAllService();
		return "done";
	}
	
	@Autowired
	EntityRepository enRepo;
	
	@Autowired
	ServiceUnitRepository suRepo;
	
	@Autowired
	CloudOfferredServiceRepository cloudRepo;
	
	@RequestMapping(method=RequestMethod.GET, value="/count")
	public String getEntity(){
		long l = enRepo.count();
		return "done: [" + l + "]---";
	}
	
	
}
