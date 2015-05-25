package at.ac.tuwien.dsg.comot.elise.collector.flexiant;

import at.ac.tuwien.dsg.comot.elise.collector.ProviderCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.FeatureType;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Provider;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Resource;

import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

public class FlexiantConnector extends ProviderCollector {

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
    final String IMAGE_RESOURCE = "ImageResource";
    final String PRODUCCE_RESOURCE_TYPE = "PRODUCCE_RESOURCE_TYPE";
    Provider flexiantProvider;
    OfferedServiceUnit vmService;

    private void enableSNIExtension() {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public FlexiantConnector() {
        super("FlexiantCollector");
        this.logger.debug("Flexiant connector");

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
        enableSNIExtension();

        this.logger.debug("Getting Flexiant UserAPI.wsdl");

        URL url = FlexiantConnector.class.getResource("/Flexiant/UserAPI.wsdl");
        UserAPI api = new UserAPI(url, new QName("http://extility.flexiant.net", "UserAPI"));
        this.service = api.getUserServicePort();

        BindingProvider portBP = (BindingProvider) this.service;
        this.logger.debug("Flexiant API endpoint before: " + this.endpoint);

        portBP.getRequestContext().put("javax.xml.ws.service.endpoint.address", this.endpoint + "/user/");
        portBP.getRequestContext().put("javax.xml.ws.security.auth.username", this.userEmailAddress + "/" + this.customerUUID);
        portBP.getRequestContext().put("javax.xml.ws.security.auth.password", this.password);

        this.logger.debug("Flexiant API endpoint: " + this.endpoint + "/user/");
        this.logger.info("Ready to query information from: " + this.siteName + "." + this.providerName);

        this.flexiantProvider = new Provider("Flexiant-unknown-site", Provider.ProviderType.IAAS);
        this.vmService = new OfferedServiceUnit("FlexiantVMService", ServiceCategory.VirtualMachine, this.flexiantProvider.getId());
        this.flexiantProvider.addOfferedServiceUnit(this.vmService);
    }

    public void updateAllService() {
        updateVMProductResource();
        updateImageResource();
    }

    public void updateImageResource() {
        enableSNIExtension();
        SearchFilter sf = new SearchFilter();
        this.logger.debug("Start to query Image information from remote Flexiant ...");

        QueryLimit lim = new QueryLimit();
        lim.setMaxRecords(40);
        try {
            ListResult result = this.service.listResources(sf, lim, ResourceType.IMAGE);
            for (Object o : result.getList()) {
                Image image = (Image) o;
                this.logger.info("Found image: " + image.getBaseName());

                Resource imageResource = new Resource("FlexiantVMResource", new FeatureType("FlexiantVMResource", FeatureType.FeatureKind.RESOURCE));
                imageResource.hasMetric(new MetricValue("baseName", image.getBaseName()))
                        .hasMetric(new MetricValue("baseUUID", image.getBaseUUID()))
                        .hasMetric(new MetricValue("clusterName", image.getClusterName()))
                        .hasMetric(new MetricValue("clusterUUID", image.getClusterUUID()))
                        .hasMetric(new MetricValue("customerName", image.getCustomerName()))
                        .hasMetric(new MetricValue("customerUUID", image.getCustomerUUID()))
                        .hasMetric(new MetricValue("imageType", image.getImageType()))
                        .hasMetric(new MetricValue("size", image.getSize()))
                        .hasMetric(new MetricValue("userPermission", image.getUserPermission()));

                this.logger.debug("Add metric done for: " + image.getBaseName());
                this.vmService.hasResource(imageResource);
            }
        } catch (ExtilityException e) {
            this.logger.error("Error when querying Flexiant resource. Error: " + e);
        }
    }

    public void updateVMProductResource() {
        enableSNIExtension();
        SearchFilter sf = new SearchFilter();

        QueryLimit lim = new QueryLimit();
        lim.setMaxRecords(40);
        try {
            ListResult result = this.service.listResources(sf, lim, ResourceType.PRODUCTOFFER);
            for (Object o : result.getList()) {
                ProductOffer po = (ProductOffer) o;
                this.logger.info("Found the product offer => " + po.getProductName());
                for (ProductComponent pc : po.getComponentConfig()) {
                    this.logger.info("Found the product component => " + pc.getComponentTypeUUID());
                    Resource vmProductResource = new Resource("VMProductResource", new FeatureType("VMProductResource", FeatureType.FeatureKind.RESOURCE));
                    for (Value v : pc.getProductConfiguredValues()) {
                        vmProductResource.hasMetric(new MetricValue(v.getKey(), v.getValue()));
                        this.logger.info("Found the config => " + v.getKey() + ":" + v.getValue());
                    }
                    this.vmService.hasResource(vmProductResource);
                }
            }
        } catch (ExtilityException e) {
            this.logger.error("Error when querying Flexiant resource. Error: " + e);
        }
    }

    private List<Nic> listAllNics() {
        List<Nic> nics = new ArrayList<>();
        try {
            QueryLimit lim = new QueryLimit();
            lim.setMaxRecords(1000);

            ListResult result = this.service.listResources(null, null, ResourceType.NIC);
            for (Object o : result.getList()) {
                Nic s = (Nic) o;
                nics.add(s);
            }
        } catch (Exception e) {
            this.logger.info(e.getMessage());
        }
        this.logger.info("Returning " + nics.size() + "number of nics ");
        return nics;
    }

    public static void main(String[] args) {
        FlexiantConnector flex = new FlexiantConnector();
        flex.sendData();
    }

    @Override
    public Provider collect() {
        updateAllService();
        return this.flexiantProvider;
    }

}
