package at.ac.tuwien.dsg.elise.extension.collectors.flexiant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;

import at.ac.tuwien.dsg.elise.Application.repositories.CloudOfferredServiceRepository;
import at.ac.tuwien.dsg.elise.Application.repositories.ServiceUnitRepository;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.CloudProvider;
import at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure.ServiceUnit;
import at.ac.tuwien.dsg.elise.settings.EliseConfiguration;

import com.extl.jade.user.ExtilityException;
import com.extl.jade.user.Image;
import com.extl.jade.user.ListResult;
import com.extl.jade.user.Nic;
import com.extl.jade.user.ProductComponent;
import com.extl.jade.user.ProductOffer;
import com.extl.jade.user.QueryLimit;
import com.extl.jade.user.ResourceType;
import com.extl.jade.user.SearchFilter;
import com.extl.jade.user.UserAPI;
import com.extl.jade.user.UserService;
import com.extl.jade.user.Value;


public class FlexiantConnector {	
	Logger logger = Logger.getLogger(FlexiantConnector.class);
	String userEmailAddress;
	String customerUUID ;
	String password;
	String endpoint;
	String vdcUUID; 
	String defaultProductOfferUUID;
	String deploymentInstanceUUID;	
	String clusterUUID;
	String networkUUID;
	String sshKey;
	
	static final String initialUser = "ubuntu";
	static final String initialPasswd = "dsg@123";
	
	static final String DEFAULT_IMAGE="a064bd97-c84c-38ef-aa37-c7391a8c8259";
	
	UserService service;
	
	ServiceUnitRepository suRepository;	
	CloudOfferredServiceRepository vmRepo;	
	
	String providerName = "openstack";
	String siteName = "unknownSite";
	
	@Bean
	GraphDatabaseService graphDatabaseService() {
		logger.debug("INITIATING DATABASE FACTORY ! 1111111111111111111111111111111111111111111");    	
	    return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
	}
	
	private void enableSNIExtension(){
		// Avoid the handshare SSL error
		System.setProperty("jsse.enableSNIExtension", "false");
	}
	
	public FlexiantConnector(String siteName, ServiceUnitRepository surepo, CloudOfferredServiceRepository vmRepo){
		logger.info("Flexiant connector");
		Properties prop = readConfig();
		this.userEmailAddress = prop.getProperty(FlexiantParameterStrings.EMAIL.getString());
		this.customerUUID = prop.getProperty(FlexiantParameterStrings.CUSTOMER_UUID.getString());
		this.password = prop.getProperty(FlexiantParameterStrings.PASSWORD.getString());
		this.endpoint = prop.getProperty(FlexiantParameterStrings.ENDPOINT.getString());
		this.vdcUUID = prop.getProperty(FlexiantParameterStrings.VDC_UUID.getString());
		this.defaultProductOfferUUID = prop.getProperty(FlexiantParameterStrings.DEFAULT_PRODUCT_OFFER_UUID.getString());
		this.clusterUUID=prop.getProperty(FlexiantParameterStrings.CLUSTER_UUID.getString());
		this.networkUUID=prop.getProperty(FlexiantParameterStrings.NETWORK_UUID.getString());
		this.sshKey = prop.getProperty(FlexiantParameterStrings.SSH_KEY.getString());
		
		this.siteName = siteName;
		this.suRepository = surepo;
		this.vmRepo = vmRepo;		
				 
		enableSNIExtension();
		
        URL url = FlexiantConnector.class.getResource("/Flexiant/UserAPI.wsdl");
        UserAPI api = new UserAPI(url, new QName("http://extility.flexiant.net", "UserAPI"));
        
        service = api.getUserServicePort();
        
        BindingProvider portBP = (BindingProvider) service;
        
        portBP.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,  this.endpoint+"/user/");
        portBP.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userEmailAddress + "/" + customerUUID);
        portBP.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

	}
	
	public void updateAllService(){
		updateVMService();
		updateImageService();
	}
	
	
	public void updateImageService(){
		CloudProvider os = new CloudProvider(siteName + "@flexiant", "IAAS");
    	suRepository.save(os);
    	
    	// create a service name VM
    	ServiceUnit imageService = new ServiceUnit();
    	imageService.name = "FlexiantImage";
    	imageService.addCapability("image");    	
    	suRepository.save(imageService);
		
		
		enableSNIExtension();
		SearchFilter sf = new SearchFilter();
		
		// Set a limit to the number of results
		QueryLimit lim = new QueryLimit();
		lim.setMaxRecords(40);
		
		try {
			// Call the service to execute the query
			ListResult result = service.listResources(sf, lim, ResourceType.IMAGE);
			
			for (Object o : result.getList()) {
				Image image = (Image)o;
				logger.info("Found image: " + image.getBaseName());
				CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "Image", image.getBaseName());
	            utility.setProvider(os);
	            utility.addDerivedServiceUnit(imageService);
	            utility.addMetaData("baseName", image.getBaseName());
	            utility.addMetaData("baseUUID", image.getBaseUUID());
	            utility.addMetaData("clusterName", image.getClusterName());
	            utility.addMetaData("clusterUUID", image.getClusterUUID());
	            utility.addMetaData("customerName", image.getCustomerName());
	            utility.addMetaData("customerUUID", image.getCustomerUUID());
	            utility.addMetaData("imageType", image.getImageType());
	            utility.addMetaData("size", image.getSize());
	            utility.addMetaData("userPermission", image.getUserPermission());	
			}
			
		} catch (ExtilityException e){
			logger.error("Error when querying Flexiant resource. Error: " + e);
		}
	}
	
	public void updateVMService(){
		CloudProvider flexiant = new CloudProvider(siteName + "@flexiant", "IAAS");
    	suRepository.save(flexiant);
    	
    	// create a service name VM
    	ServiceUnit productOfferService = new ServiceUnit();
    	productOfferService.name = "FlexiantVM";
    	productOfferService.addCapability("VM");    	
    	suRepository.save(productOfferService);
		
		
		enableSNIExtension();
		SearchFilter sf = new SearchFilter();
		
		// Set a limit to the number of results
		QueryLimit lim = new QueryLimit();
		lim.setMaxRecords(40);
		
		try {
			// Call the service to execute the query
			ListResult result = service.listResources(sf, lim, ResourceType.PRODUCTOFFER);
			
			for (Object o : result.getList()) {
				ProductOffer po = (ProductOffer)o;
				logger.info("Found the product offer => " + po.getProductName());
				CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", po.getProductName());
	            utility.setProvider(flexiant);
	            utility.addDerivedServiceUnit(productOfferService);
				for (ProductComponent pc : po.getComponentConfig()){
					logger.info("Found the product component => " + pc.getComponentTypeUUID());
					for(Value v : pc.getProductConfiguredValues()){
						logger.info("Found the config => " + v.getKey() +":" + v.getValue());
						utility.addMetaData(v.getKey(), v.getValue());
					}
				}
			}
			
		} catch (ExtilityException e){
			logger.error("Error when querying Flexiant resource. Error: " + e);
		}
	}
		

	private List<Nic> listAllNics() {
		List<Nic> nics = new ArrayList<Nic>();		
		try {
			QueryLimit lim = new QueryLimit();
			lim.setMaxRecords(1000);

			// Call the service to execute the query
			ListResult result = service.listResources(null, null, ResourceType.NIC);

			// Iterate through the results
			for (Object o : result.getList()) {
				Nic s = ((Nic) o);				
        	 	nics.add(s);

			}

		} catch (Exception e) {
			logger.info(e.getMessage()); 
		}
		logger.info("Returning "+nics.size()+"number of nics ");
		return nics;
	}
	
	
	
	public void pushAndExecuteBashScript(String ip, String username, String password, String scriptFile){
		File file = new File(scriptFile);
		try {
			logger.debug("Execute: " + "sshpass -p "+password+" scp -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null "+scriptFile+" "+username+"@"+ip+":/tmp/"+file.getName());
			int count=0;
			Process p;			
			do {				
				p = Runtime.getRuntime().exec("sshpass -p "+password+" scp -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null "+scriptFile+" "+username+"@"+ip+":/tmp/"+file.getName());
				try {
					p.waitFor();
				} catch (InterruptedException e){
					logger.error(e.getMessage());
				}
				logger.debug("Exiting value 1 ("+count++ +"): " + p.exitValue());
				try { Thread.sleep(2000); } catch (Exception e) {
					logger.error("Flexiant connector thread interrupt: " + e);
				}
			} while (p.exitValue()!=0);			
			
			Process p1;
			do {
				String cmd = "/usr/bin/sshpass -p "+password+" /usr/bin/ssh -oStrictHostKeyChecking=no -oUserKnownHostsFile=/dev/null "+username+"@"+ip+" /usr/bin/sudo /bin/bash /tmp/"+file.getName() +" > /dev/null 2>&1 & ";
		
				logger.debug("AAA:   " + cmd);
				p1 = Runtime.getRuntime().exec(cmd);
				
				try { p1.waitFor(); } catch (Exception e) {
					logger.error("Flexiant connector thread interrupt: " + e);
				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(p1.getInputStream()));
				String line="";
				while ((line = reader.readLine())!= null) {
					logger.debug(line);
				}
				
				logger.debug("Exiting value 2 ("+count++ +"): " + p1.exitValue());
				try { Thread.sleep(2000); } catch (Exception e) {
					logger.error("Flexiant connector thread interrupt: " + e);
				}
			} while (p1.exitValue()!=0);
		} catch (IOException e){
			logger.debug(e.getMessage());			
		}

	}
	
	private Properties readConfig(){
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream(EliseConfiguration.CLOUD_CONFIGURATION_STORAGE+"/"+siteName+"@flexiant");
			prop.load(input);	
		} catch (FileNotFoundException e){
			logger.error("Do not found configuration file for "+siteName+"@flexiant. Error: " + e.getMessage());
		} catch (IOException e1){
			logger.error("Cannot read configuratin file for "+siteName+"@openstack. Error: " + e1.getMessage());
		}
		return prop;
	}
	
	
}
