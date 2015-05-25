/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector.openstackvm;



import at.ac.tuwien.dsg.comot.elise.collector.UnitInstanceCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.FeatureType;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.IdentificationItem;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Resource;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.UnitInstance;

import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;

import com.google.common.collect.Multimap;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.extensions.VolumeApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.rest.RestContext;

/**
 *
 * @author hungld
 */
public class EliseCollector extends UnitInstanceCollector {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EliseCollector.class);
    NovaApi client;
    ServerApi serverApi;
    VolumeApi volumeApi;
    String keyName;
    final String region = "myregion";

    public EliseCollector() {
        super("OpenStackVMCollector");
        init();
    }

    private void init() {
        System.out.println("THIS IS ON THE SCREEN !!!");
        logger.info("Reading configuration file ...");

        String tenant = readAdaptorConfig(OpenStackParameterStrings.TENANT.getString());
        String username = readAdaptorConfig(OpenStackParameterStrings.USERNAME.getString());
        String password = readAdaptorConfig(OpenStackParameterStrings.PASSWORD.getString());
        String endpoint = readAdaptorConfig(OpenStackParameterStrings.KEYSTONE_ENDPOINT.getString());
        logger.debug("Tenant:   " + tenant);
        logger.debug("Username: " + username);
        logger.debug("Password: " + password);
        logger.debug("Endpoint: " + endpoint);

        ComputeServiceContext context = (ComputeServiceContext) ContextBuilder.newBuilder("openstack-nova").credentials(tenant + ":" + username, password).endpoint(endpoint).buildView(ComputeServiceContext.class);

        this.client = ((NovaApi) ((RestContext) context.unwrap(NovaApiMetadata.CONTEXT_TOKEN)).getApi());
        this.serverApi = this.client.getServerApiForZone("myregion");
        logger.info("Done initiation !");
    }

    @Override
    public Set<UnitInstance> collect() {
        Set<UnitInstance> instances = new HashSet<>();
        System.out.println("Servers in myregion");
        FeatureType cloudMetaType = new FeatureType("openstack-cloud-meta", FeatureType.FeatureKind.RESOURCE);
        for (Server server : this.serverApi.listInDetail().concat()) {
            logger.debug("Found server: " + server.getName());

            UnitInstance instance = new UnitInstance(server.getName(), null, null);
            instances.add(instance);

            Resource resource = new Resource("openstack-cloud-meta", cloudMetaType);

            resource.setIdByParentName(instance.getName());
            resource.hasMetric(new MetricValue("configDrive", server.getConfigDrive()))
                    .hasMetric(new MetricValue("created", server.getCreated()))
                    .hasMetric(new MetricValue("flavorName", server.getFlavor().getName()))
                    .hasMetric(new MetricValue("flavorID", server.getFlavor().getId()))
                    .hasMetric(new MetricValue("hostId", server.getHostId()))
                    .hasMetric(new MetricValue("imageID", server.getImage().getId()))
                    .hasMetric(new MetricValue("imageName", server.getImage().getName()))
                    .hasMetric(new MetricValue("keyname", server.getKeyName()))
                    .hasMetric(new MetricValue("status", server.getStatus()))
                    .hasMetric(new MetricValue("tenantId", server.getTenantId()))
                    .hasMetric(new MetricValue("updated", server.getUpdated()))
                    .hasMetric(new MetricValue("userId", server.getUserId()))
                    .hasMetric(new MetricValue("id", server.getId()));

            Multimap<String, Address> addresses = server.getAddresses();

            Collection<Address> ips = addresses.get("private");
            for (Address a : ips) {
                String ip = a.getAddr();
                logger.debug("FOUND AN ADDRESS: " + ip);
                try {
                    InetAddress inet = InetAddress.getByName(ip);
                    if (inet.isSiteLocalAddress()) {
                        logger.debug("FOUND AN LOCAL ADDRESS: " + ip);
                        resource.hasMetric(new MetricValue("privateAddress", ip));
                    } else {
                        logger.debug("FOUND AN PUBLIC ADDRESS: " + ip);
                        resource.hasMetric(new MetricValue("publicAddress", ip));
                    }
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
            }
            instance.hasResource(resource);
        }
        return instances;
    }

    @Override
    public ServiceIdentification identify(UnitInstance instance) {
        ServiceIdentification ident = new ServiceIdentification(ServiceCategory.VirtualMachine);

        String id_IPv4 = instance.findFeatureByName("openstack-cloud-meta").getMetricValueByName("privateAddress").getValue().toString();
        String id_uuid = instance.findFeatureByName("openstack-cloud-meta").getMetricValueByName("id").getValue().toString();
        logger.debug("Add identifitation. PrivateAddress: " + id_IPv4 + " and ID: " + id_uuid);
        ident.hasIdentificationItem(new IdentificationItem("privateAddress", id_IPv4, IdentificationItem.EnvIDType.IPv4, IdentificationItem.EnvIDScope.DOMAIN));
        ident.hasIdentificationItem(new IdentificationItem("id", id_uuid, IdentificationItem.EnvIDType.UUID, IdentificationItem.EnvIDScope.DOMAIN));

        instance.setIdentification(ident.toJson());
        instance.setCategory(ServiceCategory.VirtualMachine);
        return ident;
    }

    public static void main(String[] args) {
        EliseCollector osCollector = new EliseCollector();
        osCollector.sendData();
    }
}
