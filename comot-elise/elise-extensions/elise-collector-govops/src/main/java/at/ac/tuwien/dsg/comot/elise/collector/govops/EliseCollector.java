/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector.govops;

import at.ac.tuwien.dsg.comot.elise.collector.UnitInstanceCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.executionmodels.RestExecution;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.Feature;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.FeatureType;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.IdentificationItem;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;
import at.ac.tuwien.dsg.comot.model.type.State;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author hungld
 */
public class EliseCollector extends UnitInstanceCollector {

    String govoptREST = "http://128.130.172.199:8080/APIManager";

    public EliseCollector() {
        super("GovOps-unit-collector");
        this.govoptREST = readAdaptorConfig("endpoint");
    }

    @Override
    public Set<UnitInstance> collect() {
        Client orderClient = ClientBuilder.newClient();
        WebTarget target = orderClient.target(this.govoptREST + "/governanceScope/globalScope");
        GenericType<String> genericType = new GenericType<String>() {};
        String devicesJson = (String) target.request(new String[]{"application/json"}).get(genericType);

        System.out.println("Get data from GovOps in Json: " + devicesJson);

        ObjectMapper mapper = new ObjectMapper();
        List<DeviceDTO> devices;
        try {
            devices = ((DevicesDTO) mapper.readValue(devicesJson, DevicesDTO.class)).getDevices();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        Set<UnitInstance> units = new HashSet<>();
        FeatureType govops = new FeatureType("govopt-device", FeatureType.FeatureKind.METADATA);
        FeatureType metaInfoType = new FeatureType("govopt-metaInfo", FeatureType.FeatureKind.METADATA);
        for (DeviceDTO d : devices) {
            UnitInstance u = new UnitInstance(d.getName(), ServiceCategory.DEVICE, State.RUNNING);
            Feature govDevice = new Feature("govops-device", govops, Feature.LinkType.MANDATORY)
                    .hasMetric(new MetricValue("id", d.getId()))
                    .hasMetric(new MetricValue("name", d.getName()))
                    .hasMetric(new MetricValue("ip", d.getIpAddress()));

            Feature metaInfo = new Feature("govops-metaInfo", metaInfoType, Feature.LinkType.MANDATORY);
            for (Map.Entry<String, String> entry : d.getMeta().entrySet()) {
                metaInfo.hasMetric(new MetricValue((String) entry.getKey(), entry.getValue()));
            }
            govDevice.hasUnderlyingFeature(metaInfo);
            u.hasFeature(govDevice);

            String idParam = d.getId().replace(".", "_");
            System.out.println("Query to: " + this.govoptREST + "/mapper/capabilities/list/" + idParam);
            target = orderClient.target(this.govoptREST + "/mapper/capabilities/list/" + idParam);
            Invocation.Builder builder = target.request(new String[]{"application/json"});

            Response res = target.request(new String[]{"application/json"}).get();
            if (res.getStatus() < 400) {
                String capasStr = "{" + ((String) res.readEntity(String.class)).replace("}, ]", "} ]") + "}";
                System.out.println("Parsing capabilities json: " + capasStr);
                try {
                    DeviceCapabilities capas = (DeviceCapabilities) mapper.readValue(capasStr, DeviceCapabilities.class);
                    for (DeviceCapability c : capas.getCapabilities()) {
                        // only get the capability with c and end with .sh. This is the conventional from GovOps
                        if (c.getCapability().startsWith("c") && c.getCapability().endsWith(".sh")){
                            // remove the c and .sh from the name of the script
                            String capaName = c.getCapability().substring(1,c.getCapability().length()-3);
                            u.hasPrimitiveOperation(new PrimitiveOperation(capaName,  PrimitiveOperation.ExecutionMethod.REST, new RestExecution(this.govoptREST + "/invoke/" + d.getId() + "/" + c.getCapability(), RestExecution.RestMethod.GET, "")).executedBy("rtGovOps"));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            units.add(u);
        }
        return units;
    }

    @Override
    public ServiceIdentification identify(UnitInstance instance) {
        String unitID = instance.findFeatureByName("govops-device").getMetricValueByName("id").getValue().toString();
        String ip = unitID.split(":")[0].trim();
        ServiceIdentification id = new ServiceIdentification(ServiceCategory.DEVICE);
        id.hasIdentificationItem(new IdentificationItem("govops-id", unitID, IdentificationItem.EnvIDType.DomainID, IdentificationItem.EnvIDScope.DOMAIN));
        
        id.hasIdentificationItem(new IdentificationItem("ip", ip, IdentificationItem.EnvIDType.IPv4, IdentificationItem.EnvIDScope.CONTEXT));
        return id;
    }

    public static void main(String[] args) {
        EliseCollector collector = new EliseCollector();
        collector.sendData();
    }
}
