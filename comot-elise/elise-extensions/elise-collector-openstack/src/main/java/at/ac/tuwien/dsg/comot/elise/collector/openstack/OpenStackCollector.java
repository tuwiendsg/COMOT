package at.ac.tuwien.dsg.comot.elise.collector.openstack;

import at.ac.tuwien.dsg.comot.elise.collector.ProviderCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.FeatureType;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Provider;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Resource;
import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
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
import org.jclouds.rest.RestContext;

/**
 * This extension update the whole static information of a OpenStack provider This will produce a Provider with full information
 *
 * @author hungld
 */
public class OpenStackCollector extends ProviderCollector {

    static Logger logger = Logger.getLogger(OpenStackCollector.class);
    NovaApi client;
    ServerApi serverApi;
    VolumeApi volumeApi;
    String keyName;
    final String region = "myregion";
    final String FLAVOR_RESOURCE = "FlavorResource";
    final String IMAGE_RESOURCE = "ImageResource";
    final String FLOATING_IP_RESOURCE = "FloatingIPResource";
    static String id_endpoint = "";
    static String id_serviceType = "nova";
    Provider openstackProvider;
    OfferedServiceUnit vmService;

    public OpenStackCollector() {
        super("OpenStackCollector");
        init();
    }

    private void init() {
        System.out.println("THIS IS ON THE SCREEN !!!");
        logger.info("Reading configuration file ...");

        String tenant = readAdaptorConfig(OpenStackParameterStrings.TENANT.getString());
        String username = readAdaptorConfig(OpenStackParameterStrings.USERNAME.getString());
        String password = readAdaptorConfig(OpenStackParameterStrings.PASSWORD.getString());
        String endpoint = readAdaptorConfig(OpenStackParameterStrings.KEYSTONE_ENDPOINT.getString());
        String localtion = readAdaptorConfig("location");
        logger.debug("Tenant:   " + tenant);
        logger.debug("Username: " + username);
        logger.debug("Password: " + password);
        logger.debug("Endpoint: " + endpoint);
        id_endpoint = readAdaptorConfig(OpenStackParameterStrings.KEYSTONE_ENDPOINT.getString());
        logger.debug("id_endpoint: " + id_endpoint);        
        ComputeServiceContext context = (ComputeServiceContext) ContextBuilder.newBuilder("openstack-nova").credentials(tenant + ":" + username, password).endpoint(endpoint).buildView(ComputeServiceContext.class);

        this.client = ((NovaApi) ((RestContext) context.unwrap(NovaApiMetadata.CONTEXT_TOKEN)).getApi());
        this.serverApi = this.client.getServerApiForZone("myregion");
        logger.info("Done initiation !");

        logger.info("START TO FIRST SAVING DATA !");

        this.openstackProvider = new Provider("OpenStack-"+localtion, Provider.ProviderType.IAAS);
        this.vmService = new OfferedServiceUnit("OpenStackVM", ServiceCategory.VirtualMachine, this.openstackProvider.getId());
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("create", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("reboot", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("terminate", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("associateFloatingIP", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("pause", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("suspend", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.vmService.hasPrimitiveOperation(new PrimitiveOperation("createsnapshot", PrimitiveOperation.ExecutionMethod.Unknown, null));
        this.openstackProvider.addOfferedServiceUnit(this.vmService);
    }

    public void updateFlavorResource() {
        logger.debug("Getting the information about the flavor resource");
        for (Flavor f : this.client.getFlavorApiForZone("myregion").listInDetail().concat()) {
            getClass();
            Resource flavorResource = new Resource("Flavor-"+f.getName(), new FeatureType("FlavorResource", FeatureType.FeatureKind.RESOURCE));
            flavorResource.hasMetric(new MetricValue("VCPU_number", f.getVcpus()))
                    .hasMetric(new MetricValue("VCPU_speed", 2.4D))
                    .hasMetric(new MetricValue("memory", f.getRam()))
                    .hasMetric(new MetricValue("disk", f.getDisk()));

            logger.info("Name: " + f.getName() + ". ID: " + f.getId());
            if (f.getEphemeral().isPresent()) {
                flavorResource.hasMetric(new MetricValue("empheral", f.getEphemeral().get()));
            }
            logger.debug("INFOOOOOOOOOOOOOOOOOO -- DONE !");
            this.vmService.hasResource(flavorResource);
        }
    }

    public void updateImageResource() {
        logger.debug("Start to query Image information from remote OpenStack ...");
        logger.debug("Prepare to read image service");
        ImageApi imageapi = this.client.getImageApiForZone("myregion");
        for (Image image : imageapi.listInDetail().concat()) {
            logger.debug("Found image: " + image.getName());
            Resource imageResource = new Resource("Image-"+image.getName(), new FeatureType("ImageResource", FeatureType.FeatureKind.RESOURCE));
            imageResource
                    .hasMetric(new MetricValue("created", image.getCreated()))
                    .hasMetric(new MetricValue("id", image.getId()))
                    .hasMetric(new MetricValue("links", image.getLinks()))
                    .hasMetric(new MetricValue("minDisk", image.getMinDisk()))
                    .hasMetric(new MetricValue("maxRAM", image.getMinRam()))
                    .hasMetric(new MetricValue("server", image.getServer()))
                    .hasMetric(new MetricValue("status", image.getStatus()))
                    .hasMetric(new MetricValue("tenantId", image.getTenantId()))
                    .hasMetric(new MetricValue("updated", image.getUpdated()))
                    .hasMetric(new MetricValue("userId", image.getUserId()));

            this.vmService.hasResource(imageResource);
            logger.debug("Add metric done for: " + image.getName());
        }
    }

    public void updateFloatingIPResource() {
        Optional<? extends FloatingIPApi> ipApi = this.client.getFloatingIPExtensionForZone("myregion");
        logger.info("Checking FloatingIP resource to add to FloatingIp service");
        if (ipApi.isPresent()) {
            logger.info("Floating IP API is present !");
            for (FloatingIP ip : ((FloatingIPApi) ipApi.get()).list()) {
                getClass();
                Resource ipResource = new Resource("FloatingIP-"+ip.getFixedIp(), new FeatureType("FloatingIPResource", FeatureType.FeatureKind.RESOURCE));
                ipResource.hasMetric(new MetricValue("fixIP", ip.getFixedIp()))
                        .hasMetric(new MetricValue("floating_id", ip.getId()))
                        .hasMetric(new MetricValue("floating_instanceId", ip.getInstanceId()));

                this.vmService.hasResource(ipResource);
            }
        }
    }

    private void listServers() {
        for (Server server : this.serverApi.listInDetail().concat()) {
            System.out.println("  " + server);
        }
    }

    public void printServerInfo(String id) {
        System.out.println("IPv4: " + this.serverApi.get(id).getAccessIPv4());
        System.out.println("IPv6: " + this.serverApi.get(id).getAccessIPv6());
        System.out.println("hostid: " + this.serverApi.get(id).getHostId());
        System.out.println("status: " + this.serverApi.get(id).getStatus());
        System.out.println("IPv4: " + this.serverApi.get(id).getAddresses());
        Multimap<String, Address> map = this.serverApi.get(id).getAddresses();
        List<Address> PA = (List) map.get("private");
        ((Address) PA.get(0)).getAddr();
    }

    private String getIpInstance(String instanceId) {
        logger.debug("getIpInstance 1");
        Server server = this.serverApi.get(instanceId);
        logger.debug("getIpInstance 2");
        Multimap<String, Address> map = server.getAddresses();
        if (map.isEmpty()) {
            return null;
        }
        logger.debug("getIpInstance 3");
        List<Address> PA = (List) map.get("private");
        if (PA.isEmpty()) {
            return null;
        }
        logger.debug("getIpInstance 4");
        return ((Address) PA.get(0)).getAddr();
    }

    public void close()
            throws IOException {
        Closeables.close(this.client, true);
    }

    public static void main(String[] args) {
        OpenStackCollector osCollector = new OpenStackCollector();
        osCollector.sendData();
    }

    @Override
    public Provider collect() {
        logger.debug("OpenStack collector is now started ...");

        updateImageResource();
        updateFloatingIPResource();
        updateFlavorResource();
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            String os = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(openstackProvider);
            System.out.println(os);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(OpenStackCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return this.openstackProvider;
    }
}
