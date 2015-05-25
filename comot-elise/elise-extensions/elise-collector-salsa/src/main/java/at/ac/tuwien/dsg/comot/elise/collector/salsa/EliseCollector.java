/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector.salsa;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.cloud.salsa.common.processing.SalsaCenterConnector;
import at.ac.tuwien.dsg.cloud.salsa.engine.exception.SalsaEngineException;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
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

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungld
 */
public class EliseCollector extends UnitInstanceCollector {

    static Logger logger = LoggerFactory.getLogger("SALSACollector");
    String salsaREST = "http://localhost:8080/salsa-engine/rest";

    public EliseCollector() {
        super("SALSA-Unit-Collector");
        this.salsaREST = readAdaptorConfig("endpoint");
    }

    @Override
    public Set<UnitInstance> collect() {
        logger.debug("SALSA collector start to collect unit instance from SALSA !");
        Set<UnitInstance> instances = new HashSet<>();

        SalsaCenterConnector centerCon = new SalsaCenterConnector(this.salsaREST, "/tmp", logger);
        centerCon.getServiceListJson();
        Gson gson = new Gson();

        logger.debug("Collecting....");
        ServiceJsonList jsonList = (ServiceJsonList) gson.fromJson(centerCon.getServiceListJson(), ServiceJsonList.class);

        logger.debug("List all salsa service: " + jsonList.toString());
        List<String> listOfServices = new ArrayList(Arrays.asList(jsonList.toString().split(" ")));
        for (String s : listOfServices) {
            logger.debug("Checking instance on service : " + s);
            try {
                CloudService service = centerCon.getUpdateCloudServiceRuntime(s);
                for (ServiceUnit unit : service.getAllComponent()) {
                    ServiceTopology topo = service.getTopologyOfNode(unit.getId());
                    for (ServiceInstance ins : unit.getInstancesList()) {
                        logger.debug("SALSA Unit Instance: " + unit.getId() + "/" + ins.getInstanceId());
                        UnitInstance unitInst = new UnitInstance(unit.getId(), null, convertSalsaState(ins.getState()));
                        if (unit.getType().equals(SalsaEntityType.OPERATING_SYSTEM.getEntityTypeString())) {
                            unitInst.setCategory(ServiceCategory.VirtualMachine);
                        } else if (unit.getType().equals(SalsaEntityType.DOCKER.getEntityTypeString())) {
                            unitInst.setCategory(ServiceCategory.DOCKER);
                        } else if (unit.getType().equals(SalsaEntityType.TOMCAT.getEntityTypeString())) {
                            unitInst.setCategory(ServiceCategory.TOMCAT);
                        } else if (unit.getId().toLowerCase().startsWith("sensor")) {
                            unitInst.setCategory(ServiceCategory.DEVICE);
                        } else {
                            unitInst.setCategory(ServiceCategory.SOFTWARE);
                        }
                        List<at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.PrimitiveOperation> pos = ins.getPrimitive();
                        boolean existedDeploy = false;
                        boolean existedUnDeploy = false;
                        if (pos != null) {
                            for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.PrimitiveOperation po : pos) {
                                String actionName = po.getName();

                                String actionREF = this.salsaREST + "/services/" + service.getId() + "/nodes/" + unit.getId() + "/instances/" + ins.getInstanceId() + "/action_queue/" + actionName;
                                unitInst.hasPrimitiveOperation(new PrimitiveOperation(actionName, PrimitiveOperation.ExecutionMethod.REST, new RestExecution(actionREF, RestExecution.RestMethod.POST, "")).executedBy("SALSA"));
                                if (actionName.equals("deploy")) {
                                    existedDeploy = true;
                                }
                                if (actionName.equals("undeploy")) {
                                    existedUnDeploy = true;
                                }
                            }
                        }
                        Feature feature = new Feature("salsa-metadata", new FeatureType("salsa-metadata", FeatureType.FeatureKind.RESOURCE), Feature.LinkType.MANDATORY)
                                .hasMetric(new MetricValue("serviceID", service.getId()))
                                .hasMetric(new MetricValue("topologyID", topo.getId()))
                                .hasMetric(new MetricValue("unitID", unit.getId()))
                                .hasMetric(new MetricValue("instanceID", Integer.valueOf(ins.getInstanceId())))
                                .hasMetric(new MetricValue("state", ins.getState().getNodeStateString()));

                        unitInst.hasFeature(feature);

                        SalsaInstanceDescription_VM vmDescription = getVMOfUnit(service, topo, unit, ins);
                        updateFeatureFromOSNode(unitInst, vmDescription);
                        if (!existedUnDeploy) {
                            String destroyInstanceStr = this.salsaREST + "/services/" + service.getId() + "/topologies/" + topo.getId() + "/nodes/" + unit.getId() + "/instances/" + ins.getInstanceId();
                            unitInst.hasPrimitiveOperation(new PrimitiveOperation("undeploy", PrimitiveOperation.ExecutionMethod.REST, new RestExecution(destroyInstanceStr, RestExecution.RestMethod.DELETE, "")));
                        }
                        if (!existedDeploy) {
                            String deploymore = this.salsaREST + "/services/" + service.getId() + "/topologies/" + topo.getId() + "/nodes/" + unit.getId() + "/instance-count/{quantity}";
                            unitInst.hasPrimitiveOperation(new PrimitiveOperation("deploy", PrimitiveOperation.ExecutionMethod.REST, new RestExecution(deploymore, RestExecution.RestMethod.POST, "")).hasParameters("quantity", "1"));
                        }
                        logger.debug("Adding unit instance, ID: " + unitInst.getId());
                        boolean adding = instances.add(unitInst);
                        logger.debug("Added result: " + adding + ", array now have : " + instances.size());
                    }
                }
            } catch (SalsaEngineException ex) {
                ex.printStackTrace();
            }
        }
        return instances;
    }

    private void updateFeatureFromOSNode(UnitInstance inst, SalsaInstanceDescription_VM vm) {
        if (vm != null) {
            logger.debug("Updating the vm-metadata for the instance: " + inst.getName());
            Feature cloudmeta = new Feature("vm-metadata", new FeatureType("vm-metadata", FeatureType.FeatureKind.METADATA), Feature.LinkType.MANDATORY)
                    .hasMetric(new MetricValue("baseImage", vm.getBaseImage()))
                    .hasMetric(new MetricValue("instanceID", vm.getInstanceId()))
                    .hasMetric(new MetricValue("provider", vm.getProvider()))
                    .hasMetric(new MetricValue("privateIP", vm.getPrivateIp()))
                    .hasMetric(new MetricValue("publicIP", vm.getPublicIp()));
            inst.hasFeature(cloudmeta);
        } else {
            logger.debug("The VM node was not found... Serious problem !");
        }
    }

    private State convertSalsaState(SalsaEntityState salsastate) {
        switch (salsastate) {
            case ALLOCATING:
            case CONFIGURING:
            case INSTALLING:
            case STAGING:
            case STAGING_ACTION:
                return State.DEPLOYING;
            case DEPLOYED:
                return State.FINAL;
            case ERROR:
                return State.ERROR;
            case UNDEPLOYED:
                return State.UNDEPLOYING;
        }
        return State.UNDEPLOYING;
    }

    @Override
    public ServiceIdentification identify(UnitInstance instance) {
        logger.debug("Identify for unit instance: \n" + instance.toJson());
        String serviceID = instance.findFeatureByName("salsa-metadata").getValueByMetricName("serviceID").toString();
        String topoID = instance.findFeatureByName("salsa-metadata").getValueByMetricName("topologyID").toString();
        String unitID = instance.findFeatureByName("salsa-metadata").getValueByMetricName("unitID").toString();
        String instanceID = instance.findFeatureByName("salsa-metadata").getValueByMetricName("instanceID").toString();
        
        String structureID=serviceID+"/"+topoID+"/"+unitID+"/"+instanceID;
        
        String ip = instance.findFeatureByName("vm-metadata").getValueByMetricName("privateIP").toString();

        ServiceIdentification id = new ServiceIdentification(instance.getCategory());
        id.hasIdentificationItem(new IdentificationItem("salsa-id", serviceID + "/" + topoID + "/" + unitID + "/" + instanceID, IdentificationItem.EnvIDType.DomainID, IdentificationItem.EnvIDScope.DOMAIN));
        id.hasIdentificationItem(new IdentificationItem("ip", ip, IdentificationItem.EnvIDType.IPv4, IdentificationItem.EnvIDScope.CONTEXT));
        return id;
    }

    public static void main(String[] args) {

//        EliseManager eliseService = ((EliseManager) JAXRSClientFactory.create("http://localhost:8483/elise-service/rest", EliseManager.class, Collections.singletonList(new JacksonJsonProvider()))); 
//        System.out.println(eliseService.health());
//        System.out.println("STARTTTTTTTTTTTTTTT");
//        EliseCollector collector = new EliseCollector();
//        System.out.println("STARTTTTTTTTTTTTTTT 2222222222222222222");
//        Set<UnitInstance> uis = collector.collect();
//        System.out.println("STARTTTTTTTTTTTTTTT 3333333333333333333");
//        for (UnitInstance u : uis) {
//            System.out.println(u.getId());
//        }
        EliseCollector collector = new EliseCollector();
        collector.sendData();
    }

    public class ServiceJsonList {

        List<ServiceInfo> services = new ArrayList<>();

        public ServiceJsonList() {
        }

        public class ServiceInfo {

            String serviceName;
            String serviceId;
            String deployTime;

            public ServiceInfo(String name, String id, String deploytime) {
                this.serviceName = name;
                this.serviceId = id;
                this.deployTime = deploytime;
            }

            public String getServiceName() {
                return this.serviceName;
            }

            public String getServiceId() {
                return this.serviceId;
            }

            public String getDeployTime() {
                return this.deployTime;
            }
        }

        public List<ServiceInfo> getServicesList() {
            return this.services;
        }

        @Override
        public String toString() {
            String re = "";
            for (ServiceInfo si : this.services) {
                re = re + si.getServiceId() + " ";
            }
            return re.trim();
        }
    }

    private SalsaInstanceDescription_VM getVMOfUnit(CloudService service, ServiceTopology topo, ServiceUnit unit, ServiceInstance instance) {
        logger.debug("Getting VM for node: " + service.getId() + "/" + topo.getId() + "/" + unit.getId() + "/" + instance.getInstanceId());
        if (unit.getType().equals(SalsaEntityType.OPERATING_SYSTEM.getEntityTypeString())) {
            logger.debug("The node is acually the OS, return !");
            return (SalsaInstanceDescription_VM) instance.getProperties().getAny();
        }
        ServiceUnit hostedUnit = topo.getComponentById(unit.getHostedId());
        ServiceInstance hostedInstance = hostedUnit.getInstanceById(instance.getHostedId_Integer());
        while (!hostedUnit.getType().equals(SalsaEntityType.OPERATING_SYSTEM.getEntityTypeString())) {
            logger.debug("Not a OS node, checking which hosts this node...");
            hostedUnit = topo.getComponentById(hostedUnit.getHostedId());
            hostedInstance = hostedUnit.getInstanceById(hostedInstance.getHostedId_Integer());
            logger.debug("And we found the host node is: " + hostedUnit.getId() + "/" + hostedInstance.getInstanceId());
        }
        logger.debug("IN conclude, instance is hosted on the OS node: " + hostedUnit.getId() + "/" + hostedInstance.getInstanceId());
        return (SalsaInstanceDescription_VM) hostedInstance.getProperties().getAny();
    }
}
