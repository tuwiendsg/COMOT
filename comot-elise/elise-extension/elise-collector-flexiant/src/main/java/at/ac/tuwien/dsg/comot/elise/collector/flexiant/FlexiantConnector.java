package at.ac.tuwien.dsg.comot.elise.collector.flexiant;

import at.ac.tuwien.dsg.comot.elise.collector.GenericCollector;
import at.ac.tuwien.dsg.comot.model.provider.Entity;
import at.ac.tuwien.dsg.comot.model.provider.Metric;
import at.ac.tuwien.dsg.comot.model.provider.MetricValue;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Provider;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
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

public class FlexiantConnector extends GenericCollector{

    Logger logger = Logger.getLogger(FlexiantConnector.class);
    String userEmailAddress;
    String customerUUID;
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

    static final String DEFAULT_IMAGE = "a064bd97-c84c-38ef-aa37-c7391a8c8259";

    UserService service;


    String providerName = "Flexiant";
    String siteName = "unknownSite";

    Provider provider;
    OfferedServiceUnit vmService;

        
    private void enableSNIExtension() {
        // Avoid the handshare SSL error
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public FlexiantConnector() {
        super();
        logger.debug("Flexiant connector");
       
        this.userEmailAddress = readAdaptorConfig(FlexiantParameterStrings.EMAIL.getString());
        this.customerUUID = readAdaptorConfig(FlexiantParameterStrings.CUSTOMER_UUID.getString());
        this.password = readAdaptorConfig(FlexiantParameterStrings.PASSWORD.getString());
        this.endpoint = readAdaptorConfig(FlexiantParameterStrings.ENDPOINT.getString());
        this.vdcUUID = readAdaptorConfig(FlexiantParameterStrings.VDC_UUID.getString());
        this.defaultProductOfferUUID = readAdaptorConfig(FlexiantParameterStrings.DEFAULT_PRODUCT_OFFER_UUID.getString());
        this.clusterUUID = readAdaptorConfig(FlexiantParameterStrings.CLUSTER_UUID.getString());
        this.networkUUID = readAdaptorConfig(FlexiantParameterStrings.NETWORK_UUID.getString());
        this.sshKey = readAdaptorConfig(FlexiantParameterStrings.SSH_KEY.getString());

        this.siteName = readAdaptorConfig("name");
//        this.entityRepo = entityRepo;
//        this.cloudOfferedServiceRepo = cloudOfferedServiceRepo;

        enableSNIExtension();

        logger.debug("Getting Flexiant UserAPI.wsdl");
        
        URL url = FlexiantConnector.class.getResource("/Flexiant/UserAPI.wsdl");
        logger.debug("1");
        UserAPI api = new UserAPI(url, new QName("http://extility.flexiant.net", "UserAPI"));
        logger.debug("2");

        service = api.getUserServicePort();
        logger.debug("3");

        BindingProvider portBP = (BindingProvider) service;
        logger.debug("Flexiant API endpoint before: " + this.endpoint);
        
        portBP.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.endpoint + "/user/");
        portBP.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userEmailAddress + "/" + customerUUID);
        portBP.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
        
        logger.debug("Flexiant API endpoint: " + this.endpoint +"/user/");

        // OK, now save first the cloud provider and services as they are fixed
        // init data for cloud provider and its services
        provider = new Provider(siteName + "." + providerName, Provider.ProviderType.IAAS);
        this.vmService = new OfferedServiceUnit("VirtualInfrastructure", provider.getId(), "IaaS", "VM");
        
        provider.addOfferedServiceUnit(this.vmService);
        
        logger.info("A new provider created: " + siteName + "." + providerName);

    }

    public void updateAllService() {
        updateVMService();
        updateImageService();
    }

    public void updateImageService() {
        enableSNIExtension();
        SearchFilter sf = new SearchFilter();
        logger.debug("Start to query Image information from remote Flexiant ...");

        // Set a limit to the number of results
        QueryLimit lim = new QueryLimit();
        lim.setMaxRecords(40);
        
        ResourceOrQualityType imageResourceType = new ResourceOrQualityType("ImageResource")
                .hasMetric(new Metric("baseName", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("baseUUID", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("clusterName", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("clusterUUID", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("customerName", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("customerUUID", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("imageType", "text", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("size", "GB", Metric.MetricType.RESOURCE))
                .hasMetric(new Metric("userPermission", "text", Metric.MetricType.RESOURCE));
        
//        entityRepo.save(imageResourceType);
        
        try {
            // Call the service to execute the query
            ListResult result = service.listResources(sf, lim, ResourceType.IMAGE);

            for (Object o : result.getList()) {                
                Image image = (Image) o;
                logger.info("Found image: " + image.getBaseName());
                Resource imageRs = new Resource(image.getBaseName(), imageResourceType)
                        .hasMetric(new MetricValue("baseName", image.getBaseName(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("baseUUID", image.getBaseUUID(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("clusterName", image.getClusterName(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("clusterUUID", image.getClusterUUID(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("customerName", image.getCustomerName(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("customerName", image.getCustomerUUID(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("imageType", image.getImageType(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("size", image.getSize(), MetricValue.ValueType.TEXT))
                        .hasMetric(new MetricValue("userPermission", image.getUserPermission(), MetricValue.ValueType.TEXT));                        
                logger.info("Add metric done for: " + image.getBaseName());
                // add flavor to the VM service and save to DB
                vmService.hasResource(imageRs);
//                entityRepo.save(imageRs);
            }
//            cloudOfferedServiceRepo.save(vmService);

        } catch (ExtilityException e) {
            logger.error("Error when querying Flexiant resource. Error: " + e);
        }
    }

    public void updateVMService() {
        enableSNIExtension();
        SearchFilter sf = new SearchFilter();

        // Set a limit to the number of results
        QueryLimit lim = new QueryLimit();
        lim.setMaxRecords(40);

        try {
            // Call the service to execute the query
            ListResult result = service.listResources(sf, lim, ResourceType.PRODUCTOFFER);
            
            ResourceOrQualityType productionRsType = new ResourceOrQualityType("ProductionResource");
            

            for (Object o : result.getList()) {
                ProductOffer po = (ProductOffer) o;
                logger.info("Found the product offer => " + po.getProductName());
                Resource productionRs = new Resource(po.getProductName(), productionRsType);                
                        
                for (ProductComponent pc : po.getComponentConfig()) {
                    logger.info("Found the product component => " + pc.getComponentTypeUUID());
                    for (Value v : pc.getProductConfiguredValues()) {
                        Metric metric = new Metric(v.getKey(), "metadata", Metric.MetricType.RESOURCE);
                        productionRsType.hasMetric(metric);
                        productionRs.hasMetric(new MetricValue(metric.getName(), v.getValue(), MetricValue.ValueType.TEXT));
                        logger.info("Found the config => " + v.getKey() + ":" + v.getValue());                        
                    }
                    vmService.hasResource(productionRs);
//                    entityRepo.save(productionRs);
                }
            }
//            entityRepo.save(productionRsType);
//            cloudOfferedServiceRepo.save(vmService);

        } catch (ExtilityException e) {
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
        logger.info("Returning " + nics.size() + "number of nics ");
        return nics;
    }

    @Override
    public Entity collect() {
        updateAllService();
        return this.provider;
    }
    
    public static void main(String[] args){
        FlexiantConnector flex = new FlexiantConnector();
        flex.sendData();
    }

}
