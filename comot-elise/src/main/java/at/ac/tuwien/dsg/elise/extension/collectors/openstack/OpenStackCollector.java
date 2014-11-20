package at.ac.tuwien.dsg.elise.extension.collectors.openstack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.extensions.VolumeApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;

import at.ac.tuwien.dsg.elise.Application.repositories.CloudOfferredServiceRepository;
import at.ac.tuwien.dsg.elise.Application.repositories.ServiceUnitRepository;
import at.ac.tuwien.dsg.elise.concepts.PrimitiveOperation;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.CloudOfferedServiceUnit;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.CloudProvider;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.Resource;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.MetricValue;
import at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure.ServiceUnit;
import at.ac.tuwien.dsg.elise.settings.EliseConfiguration;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;

public class OpenStackCollector{
	Logger logger = Logger.getLogger(OpenStackCollector.class);
	NovaApi client;
	ServerApi serverApi;
	VolumeApi volumeApi;	
	String keyName;	
	final String region = "myregion";
	Map<String, String> mapFlavorName = new HashMap<String, String>();
	
    ServiceUnitRepository suRepository;	
    CloudOfferredServiceRepository vmRepo;	
    
    String providerName = "openstack";
    String siteName = "unknownSite";
    
    @Bean
    GraphDatabaseService graphDatabaseService() {
    	logger.debug("INITIATING DATABASE FACTORY ! 1111111111111111111111111111111111111111111");    	
        return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
    }
	
	@SuppressWarnings("deprecation")
	public OpenStackCollector(String siteName, ServiceUnitRepository surepo, CloudOfferredServiceRepository vmRepo){
		logger.info("OpenStackCollector: " + ". Site Name:" + siteName);
		logger.info("YOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
		this.siteName = siteName;
		this.suRepository = surepo;
		this.vmRepo = vmRepo;		
		Properties prop = readConfig();
		String tenant = prop.getProperty(OpenStackParameterStrings.TENANT.getString());
		String username = prop.getProperty(OpenStackParameterStrings.USERNAME.getString());
		String password = prop.getProperty(OpenStackParameterStrings.PASSWORD.getString());
		String endpoint = prop.getProperty(OpenStackParameterStrings.KEYSTONE_ENDPOINT.getString());
		logger.debug(tenant);
		logger.debug(username);
		logger.debug(password);
		logger.debug(endpoint);
		ComputeServiceContext context = ContextBuilder.newBuilder("openstack-nova")
                .credentials(tenant + ":" + username, password)
                .endpoint(endpoint)
                .buildView(ComputeServiceContext.class);		
	        client = (NovaApi) context.unwrap(NovaApiMetadata.CONTEXT_TOKEN).getApi();
	        serverApi = client.getServerApiForZone(region);
	        this.siteName = siteName;	  
	    logger.debug("Done initiation");
	}
	
	
	private Properties readConfig(){
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream(EliseConfiguration.CLOUD_CONFIGURATION_STORAGE+"/"+siteName+"@openstack");
			prop.load(input);	
		} catch (FileNotFoundException e){
			logger.error("Do not found configuration file for dsg@openstack. Error: " + e.getMessage());
		} catch (IOException e1){
			logger.error("Cannot read configuratin file for dsg@openstack. Error: " + e1.getMessage());
		}
		return prop;
	}
	
	public void updateAllService(){
		//addResource();
		updateVMService();
		updateImageService();
		updateFloatingIPService();
	}
	
	Map<String, Resource> resourceMap = new HashMap<String, Resource>();
	public void addResource(){
		logger.debug("Adding resources type for openstack");
		// resource of the cloud provider
    	Resource rComputing = new Resource("Computing");
    	Resource rMemory = new Resource("Memory");
    	Resource rRootDisk = new Resource("RootDisk");
    	Resource rEphemeralDisk = new Resource("EphemeralDisk");
    	resourceMap.put("computing", rComputing);
    	resourceMap.put("memory", rMemory);
    	resourceMap.put("disk", rRootDisk);
    	resourceMap.put("ephemeral", rEphemeralDisk);
//        enRepo.save(rComputing);        
//        enRepo.save(rMemory);        
//        enRepo.save(rRootDisk);
//        enRepo.save(rEphemeralDisk);
        logger.debug("Done adding resources type for openstack");
	}
	
	public void updateVMService(){
		final String RESOURCE_COMPUTING="VCPU";
		final String RESOURCE_MEMORY="RAM";
		final String RESOURCE_DISK="Disk";
		final String RESOURCE_EMPHERAL="Empheral";
		// create provider
		CloudProvider os = new CloudProvider("dsg@openstack", "IAAS");    	
    	suRepository.save(os);
    	
    	// create a service name VM
    	ServiceUnit osVm = new ServiceUnit();
    	osVm.name = "OpenstackVM";
    	osVm.addCapability("vm");
    	osVm.addPrimitiveOperation(PrimitiveOperation.newSalsaConnector("start", "openstack"));
    	osVm.addPrimitiveOperation(PrimitiveOperation.newSalsaConnector("stop", "openstack"));
    	osVm.addPrimitiveOperation(PrimitiveOperation.newSalsaConnector("addFloatIP", "openstack"));
    	suRepository.save(osVm);
    	logger.debug("Start to query service information from remote OpenStack");
		// create flavor service
		for (Flavor f : client.getFlavorApiForZone(region).listInDetail().concat()) {
			CloudOfferedServiceUnit unit = new CloudOfferedServiceUnit();
			unit.setName(this.providerName + "." + this.siteName + "." + f.getId());
			unit.setProvider(os);	
			
			logger.debug("Name: " + f.getName() +". ID: "+ f.getId());
			
            CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "VM", f.getName());
            utility.addDerivedServiceUnit(osVm);
            utility.setProvider(os);            
            
            logger.debug("Name: " + f.getName() +". ID: "+ f.getId());
            logger.debug("DEBUGGGGGG---YAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            logger.info("INFOOOOOOOOOOOOOOOOOO");
            //vm resource: computing
            {               
                Resource resource = new Resource(RESOURCE_COMPUTING);
                resource.addProperty(new Metric("VCPU", "number"), new MetricValue(f.getVcpus()));    
                resource.addProperty(new Metric("speed", "number"), new MetricValue(2.4));
                utility.addResourceProperty(resource);
            }

            //vm resource: memory
            {
            	Resource resource = new Resource(RESOURCE_MEMORY);            
                resource.addProperty(new Metric("size", "MB"), new MetricValue(f.getRam()));
                utility.addResourceProperty(resource);
            }
            //vm resource: RootDisk
            {
            	Resource resource = new Resource(RESOURCE_DISK);            
                resource.addProperty(new Metric("size", "GB"), new MetricValue(f.getDisk()));
                utility.addResourceProperty(resource);
            }
            //vm resource: EphemeralDisk ==> Optional, so need to be checked for null
            {
            	if (f.getEphemeral().isPresent()){
	            	Resource resource = new Resource(RESOURCE_EMPHERAL);            
	                resource.addProperty(new Metric("size", "GB"), new MetricValue(f.getEphemeral().get()));
	                utility.addResourceProperty(resource);
            	}
            }            
            vmRepo.save(utility);
        }		
	}
	
	public void updateImageService(){
		// create provider
		CloudProvider os = new CloudProvider("dsg@openstack", "IAAS");
    	suRepository.save(os);
    	
    	// create a service name VM
    	ServiceUnit imageService = new ServiceUnit();
    	imageService.name = "OpenstackImage";
    	imageService.addCapability("image");    	
    	suRepository.save(imageService); 	
    	
		// insert images
    	logger.info("Prepare to read image service");
		ImageApi imageapi = client.getImageApiForZone(region);		
		for (Image image : imageapi.listInDetail().concat()){
			logger.info("Found image: " + image.getName());
			CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "Image", image.getName());
            utility.setProvider(os);
            utility.addDerivedServiceUnit(imageService);
			utility.addMetaData("created", image.getCreated());
			utility.addMetaData("id", image.getId());
			utility.addMetaData("links", image.getLinks());
			utility.addMetaData("minDisk", image.getMinDisk());
			utility.addMetaData("maxRAM", image.getMinRam());
			utility.addMetaData("server", image.getServer());
			utility.addMetaData("status", image.getStatus());
			utility.addMetaData("tenantId", image.getTenantId());
			utility.addMetaData("updated", image.getUpdated());
			utility.addMetaData("userId", image.getUserId());
			vmRepo.save(utility);
		}
	}
	
	public void updateFloatingIPService(){
		// create provider
		CloudProvider os = new CloudProvider("dsg@openstack", "IAAS");
    	suRepository.save(os);
    	
    	// create a service name VM
    	ServiceUnit floatingIP = new ServiceUnit();
    	floatingIP.name = "OpenstackFloatingIP";
    	floatingIP.addCapability("floatingIP");
    	suRepository.save(floatingIP);
    	
		Optional<? extends FloatingIPApi> ipApi = client.getFloatingIPExtensionForZone(region);
		logger.info("Preparing to add FloatingIP");
		if (ipApi.isPresent()){
			logger.info("Floating IP API is present");
			for(FloatingIP ip : ipApi.get().list()) {
				logger.info("Get IP: " + ip.getIp());
				CloudOfferedServiceUnit utility = new CloudOfferedServiceUnit("VirtualInfrastructure", "FloatingIP", ip.getIp());
	            utility.setProvider(os);
	            utility.addDerivedServiceUnit(floatingIP);
				utility.addMetaData("fixIP", ip.getFixedIp());
				utility.addMetaData("id", ip.getId());
				utility.addMetaData("instanceId", ip.getInstanceId());
				vmRepo.save(utility);
			}
		}
	}
	
	
	public void listServers() {
		for (Server server : serverApi.listInDetail().concat()) {
                System.out.println("  " + server);
		}        
    }
	
	public void printServerInfo(String id){
		System.out.println("IPv4: " + serverApi.get(id).getAccessIPv4());
		System.out.println("IPv6: " + serverApi.get(id).getAccessIPv6());
		System.out.println("hostid: " + serverApi.get(id).getHostId());
		System.out.println("status: " + serverApi.get(id).getStatus());		
		System.out.println("IPv4: " + serverApi.get(id).getAddresses());
		Multimap<String, Address> map = serverApi.get(id).getAddresses();
		List<Address> PA = (List<Address>) map.get("private");
		PA.get(0).getAddr();		
	}
	
	private String getIpInstance(String instanceId){
		logger.debug("getIpInstance 1");
		Server server = serverApi.get(instanceId);
		logger.debug("getIpInstance 2");
		Multimap<String, Address> map = server.getAddresses();
		if (map.isEmpty()){
			return null;
		}
		logger.debug("getIpInstance 3");
		List<Address> PA = (List<Address>) map.get("private");

		if (PA.isEmpty()){
			return null;							
		}
		logger.debug("getIpInstance 4");
		return PA.get(0).getAddr();
	}
	
	
	 public void close() throws IOException {
	        Closeables.close(client, true);
	 }
}
