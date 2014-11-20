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
import at.ac.tuwien.dsg.elise.Application.repositories.ServiceUnitRepository;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.Resource;
import at.ac.tuwien.dsg.elise.extension.collectors.flexiant.FlexiantConnector;
import at.ac.tuwien.dsg.elise.extension.collectors.openstack.OpenStackCollector;

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
		DataGeneration gen = new DataGeneration(suRepo, cloudRepo);
		gen.generateAll();
		return "done";
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/cloudprovider/DSGOpenStack/update")
	public String updateDSGOpenstack(){
		//logger.debug("dsg" +" - " + prop.getProperty(OpenStackParameterStrings.END_POINT.getString())+" - " + OpenStackParameterStrings.TENANT.getString() +" - " + OpenStackParameterStrings.USERNAME.getString() +" - " + OpenStackParameterStrings.PASSWORD.getString() +" - " + OpenStackParameterStrings.SSH_KEY_NAME.getString());
		OpenStackCollector openstack = new OpenStackCollector("dsg", suRepo, cloudRepo);
		openstack.updateAllService();
		return "done";
	}
	
//	@RequestMapping(method=RequestMethod.POST, value="/cloudprovider/CelarFlexiant/update")
//	public String updateCelarFlexiant(){
//		FlexiantConnector flexiant = new FlexiantConnector("celar", suRepo, cloudRepo);
//		flexiant.updateAllService();		
//		return "done";
//	}
	

	
	@RequestMapping(method=RequestMethod.POST, value="/servicedefinition")
	public void addServiceDefinitionByTOSCA(String tosca){
		
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/servicetemplate")
	public void addServiceTemplate(){
		
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/serviceinstance")
	public void addServiceInstance(){
		
	}
	
	@Autowired
	ServiceUnitRepository suRepo;
	
	@Autowired
	CloudOfferredServiceRepository cloudRepo;
	
	@RequestMapping(method=RequestMethod.GET, value="/count")
	public String getEntity(){
		long l = suRepo.count();
		return "done: [" + l + "]---";
	}
	
	
	@RequestMapping(method=RequestMethod.GET, value="/testyo")
	public String testyo(){
		String str = "";
		CloudOfferedServiceUnit vm = cloudRepo.findByName("dsg@openstack/w1.xlarge");
		str+= vm.getName() +"-\n-";
		str+= vm.getCategory() +"-\n-";
		str+= vm.getSubcategory() +"-\n-";
		str+= vm.getType() +"-\n-";
		str+= vm.getResourceProperties() +"-\n-";
		for (Resource re : vm.getResourceProperties()) {
			str += re.toString() + "\n";
		}
		
		return str;		
	}
	
}
