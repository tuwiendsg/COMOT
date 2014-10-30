package at.ac.tuwien.dsg.ElasticityInformationService.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExtendServices {
	@Autowired
	DataServiceGeneration dataGenerator;
	
	@RequestMapping("/elise/health")
	public String health(){
		return "Hello Elise !";
	}
	
	@RequestMapping("/elise/generatedata")
	public String generateData(){
		dataGenerator.generateAll();
		return "";
	}
}
