package at.ac.tuwien.dsg.comot.elise.collector.openstack;

import at.ac.tuwien.dsg.comot.model.offeredserviceunit.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Provider;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Metric;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.MetricValue;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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

import at.ac.tuwien.dsg.comot.elise.common.DataProviderInterface;



import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Entity;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.ResourceOrQualityType;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.google.gson.Gson;

/**
 * This extension update the whole static information of a OpenStack provider
 * This will produce a Provider with full information
 * @author hungld
 */
public class OpenStackCollector implements DataProviderInterface{

    Logger logger = Logger.getLogger(OpenStackCollector.class);
    NovaApi client;
    ServerApi serverApi;
    VolumeApi volumeApi;
    String keyName;
    final String region = "myregion";

    String providerName = "openstack";

    // list of things that this support
    Provider provider;
    OfferedServiceUnit vmService;
    OfferedServiceUnit floatingIPService;
    
//    public OpenStackCollector(){}

    public OpenStackCollector() {
        System.out.println("THIS IS ON THE SCREEN !!!");        
        logger.info("Reading configuration file ...");
        Properties prop = readConfig();
        String siteName = prop.getProperty("name");
        logger.info("OpenStackCollector: " + ". Site Name:" + siteName);
        String tenant = prop.getProperty(OpenStackParameterStrings.TENANT.getString());
        String username = prop.getProperty(OpenStackParameterStrings.USERNAME.getString());
        String password = prop.getProperty(OpenStackParameterStrings.PASSWORD.getString());
        String endpoint = prop.getProperty(OpenStackParameterStrings.KEYSTONE_ENDPOINT.getString());        
        logger.debug("Tenant:   "+tenant);
        logger.debug("Username: "+username);
        logger.debug("Password: "+password);
        logger.debug("Endpoint: "+endpoint);        
        ComputeServiceContext context = ContextBuilder.newBuilder("openstack-nova")
                .credentials(tenant + ":" + username, password)
                .endpoint(endpoint)
                .buildView(ComputeServiceContext.class);
        client = (NovaApi) context.unwrap(NovaApiMetadata.CONTEXT_TOKEN).getApi();
        serverApi = client.getServerApiForZone(region);
        logger.info("Done initiation !");

        logger.info("START TO FIRST SAVING DATA !");
        // init data for cloud provider and its services
        provider = new Provider(siteName + "." + providerName, Provider.ProviderType.IAAS);  
        // TODO: Change to naming service
        provider.setId(siteName+"."+providerName);
        logger.info("Provider created: " + provider.getId());
        this.vmService = new OfferedServiceUnit("VirtualInfrastructure", provider.getId(), "IaaS", "VM");
        this.floatingIPService = new OfferedServiceUnit("FloatingIP", provider.getId(), "IaaS", "VM");   
        provider.addOfferedServiceUnit(vmService);
        provider.addOfferedServiceUnit(floatingIPService);
        
        logger.info("A new provider created: " + siteName + "." + providerName);
        logger.info("Having following offering: ");        
        for (OfferedServiceUnit u:provider.getOffering()){
            System.out.println(u.getId());
        }
    }
    
    
    
    public String exportProviderDescription(){
        Gson gson = new Gson();
        return gson.toJson(this.provider);        
    }
            
            
    public Properties readConfig() {
        Properties prop = new Properties();
        final String CURRENT_DIR = System.getProperty("user.dir");
        final String DATA_PROVIDER_CONFIG_FILE = CURRENT_DIR+"/openstack.conf";
        try {
            InputStream input = new FileInputStream(DATA_PROVIDER_CONFIG_FILE);
            prop.load(input);
        } catch (FileNotFoundException e) {
            logger.error("Do not found configuration file for dsg@openstack. Error: " + e.getMessage());
        } catch (IOException e1) {
            logger.error("Cannot read configuratin file for dsg@openstack. Error: " + e1.getMessage());
        }
        return prop;
    }

    public void updateAllService() {
        System.out.println("START TO UPDATE FLAVOR !");
        updateFlavorResource();
        updateImageService();
        updateFloatingIPService();
    }

    public void updateFlavorResource() {
        logger.debug("Start to query Flavor information from remote OpenStack ...");
        // create set of metric for flavor
        ResourceOrQualityType flavorType = new ResourceOrQualityType("FlavorResource")
                .hasMetric(new Metric("VCPU_number", "core", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("VCPU_speed", "GHz", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("memory", "MB", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("disk", "GB", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("empheral", "GB", Metric.MetricType.RESOURCE));

//        entityRepo.save(flavorType);

        // create flavor service
//        int count=0;
        for (Flavor f : client.getFlavorApiForZone(region).listInDetail().concat()) {
//            count ++;
//            if (count >5){
//                break;
//            }
            logger.info("Name: " + f.getName() + ". ID: " + f.getId());
            // create a resource
            Resource vmFlavor = new Resource(f.getName(), flavorType);
            // add to the cloud service            
            // add metric property to the resource

            vmFlavor.hasMetric(new MetricValue("VCPU_number", f.getVcpus(), MetricValue.ValueType.NUMERIC))
                    .hasMetric(new MetricValue("VCPU_speed", 2.4, MetricValue.ValueType.NUMERIC))
                    .hasMetric(new MetricValue("memory", f.getRam(), MetricValue.ValueType.NUMERIC))
                    .hasMetric(new MetricValue("disk", f.getDisk(), MetricValue.ValueType.NUMERIC));
            
            if (f.getEphemeral().isPresent()) {
                vmFlavor.hasMetric(new MetricValue("empheral", f.getEphemeral().get(), MetricValue.ValueType.NUMERIC));
            }
            
            vmService.hasResource(vmFlavor);

            logger.info("INFOOOOOOOOOOOOOOOOOO");
            logger.info("vmservice: " + vmService.getId() + "---vmFlavor: " + vmFlavor.getName());
            
         
        }
    }

    public void updateImageService() {
        logger.debug("Start to query Image information from remote OpenStack ...");

        ResourceOrQualityType imageType = new ResourceOrQualityType("ImageResource")
                .hasMetric(new Metric("created", "date", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("id", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("links", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("minDisk", "GB", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("maxRAM", "MB", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("status", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("server", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("tenantId", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("updated", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("userId", "text", Metric.MetricType.RESOURCE));

//        entityRepo.save(imageType);

        // insert images
        logger.info("Prepare to read image service");
        ImageApi imageapi = client.getImageApiForZone(region);
        for (Image image : imageapi.listInDetail().concat()) {
            logger.info("Found image: " + image.getName());

            Resource imageRs = new Resource(image.getName(), imageType)
                    .hasMetric(new MetricValue("created", image.getCreated(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("id", image.getId(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("links", image.getLinks(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("minDisk", image.getMinDisk(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("maxRAM", image.getMinRam(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("server", image.getServer(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("status", image.getStatus(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("tenantId", image.getTenantId(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("updated", image.getUpdated(), MetricValue.ValueType.TEXT))
                    .hasMetric(new MetricValue("userId", image.getUserId(), MetricValue.ValueType.TEXT));

            logger.info("Add metric done for: " + image.getName());
            // add flavor to the VM service and save to DB
            vmService.hasResource(imageRs);
            // save one image
//            entityRepo.save(imageRs);
        }
        // update VM service
        //cloudOfferedServiceRepo.save(vmService);
    }

    public void updateFloatingIPService() {
        Optional<? extends FloatingIPApi> ipApi = client.getFloatingIPExtensionForZone(region);
        logger.info("Checking FloatingIP resource to add to FloatingIp service");
        ResourceOrQualityType floatingIPType = new ResourceOrQualityType("FloatingIPResource")
                .hasMetric(new Metric("fixIP", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("floating_id", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("floating_instanceId", "text", Metric.MetricType.RESOURCE));

//        entityRepo.save(floatingIPType);

        if (ipApi.isPresent()) {
            logger.info("Floating IP API is present !");
            for (FloatingIP ip : ipApi.get().list()) {
//                logger.info("Get IP: " + ip.getIp());
                Resource floatingIpRs = new Resource(ip.getIp(), floatingIPType)
                        .hasMetric(new MetricValue("fixIP", ip.getFixedIp(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("floating_id", ip.getId(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("floating_instanceId", ip.getInstanceId(), MetricValue.ValueType.TEXT));

                floatingIPService.hasResource(floatingIpRs);
//                entityRepo.save(floatingIpRs);
            }
        }
        // update VM service
        //cloudOfferedServiceRepo.save(vmService);
    }

    public void listServers() {
        for (Server server : serverApi.listInDetail().concat()) {
            System.out.println("  " + server);
        }
    }

    public void printServerInfo(String id) {
        System.out.println("IPv4: " + serverApi.get(id).getAccessIPv4());
        System.out.println("IPv6: " + serverApi.get(id).getAccessIPv6());
        System.out.println("hostid: " + serverApi.get(id).getHostId());
        System.out.println("status: " + serverApi.get(id).getStatus());
        System.out.println("IPv4: " + serverApi.get(id).getAddresses());
        Multimap<String, Address> map = serverApi.get(id).getAddresses();
        List<Address> PA = (List<Address>) map.get("private");
        PA.get(0).getAddr();
    }

    private String getIpInstance(String instanceId) {
        logger.debug("getIpInstance 1");
        Server server = serverApi.get(instanceId);
        logger.debug("getIpInstance 2");
        Multimap<String, Address> map = server.getAddresses();
        if (map.isEmpty()) {
            return null;
        }
        logger.debug("getIpInstance 3");
        List<Address> PA = (List<Address>) map.get("private");

        if (PA.isEmpty()) {
            return null;
        }
        logger.debug("getIpInstance 4");
        return PA.get(0).getAddr();
    }

    public void close() throws IOException {
        Closeables.close(client, true);
    }

    @Override
    public Entity readData() {
        updateAllService();
        return this.provider;
    }
}
