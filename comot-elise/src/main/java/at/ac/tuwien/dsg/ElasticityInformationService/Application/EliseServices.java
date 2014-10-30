package at.ac.tuwien.dsg.ElasticityInformationService.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.CloudOfferredServiceRepository;
import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.EntityRepository;
import at.ac.tuwien.dsg.ElasticityInformationService.Application.repositories.ServiceUnitRepository;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;

@RestController
@Controller
@Component
@RequestMapping("/elise")
public class EliseServices {
	
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
