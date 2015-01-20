/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.orchestrator.interraction.rsybl;

import at.ac.tuwien.dsg.comot.common.model.AbstractCloudEntity;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CapabilityEffect;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.RelationshipXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.SYBLAnnotationXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.ServiceTopologyXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.ServiceUnitXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.URI;
import java.util.EnumMap;
import java.util.logging.Level;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class rSYBLInterraction {

    private static final Logger log = LoggerFactory.getLogger(rSYBLInterraction.class);

    private String rSYBL_BASE_IP = "128.130.172.214";
    private Integer rSYBL_BASE_PORT = 8081;
    private String rSYBL_BASE_URL = "/rSYBL/restWS";

    public void sendInitialConfigToRSYBL(CloudService serviceTemplate, DeploymentDescription deploymentDescription, CompositionRulesConfiguration compositionRulesConfiguration, String effectsJSON) {

        deploymentDescription = enrichWithElasticityCapabilities(deploymentDescription, serviceTemplate);

        HttpHost endpoint = new HttpHost(rSYBL_BASE_IP, rSYBL_BASE_PORT);

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI prepareConfigURI = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/prepareControl").build();
            HttpPut prepareConfig = new HttpPut(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(DeploymentDescription.class);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter sw = new StringWriter();

                log.info("Sending deployment description to rSYBL");
                marshaller.marshal(deploymentDescription, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/deployment").build();
                HttpPut putDeployment = new HttpPut(putDeploymentStructureURL);

                StringEntity entity = new StringEntity(sw.getBuffer().toString());

                entity.setContentType("application/xml");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Executing request " + putDeployment.getRequestLine());
                HttpResponse response = httpClient.execute(endpoint, putDeployment);
                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CloudServiceXML.class);
                CloudServiceXML cloudServiceXML = toRSYBLRepresentation(serviceTemplate);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                StringWriter sw = new StringWriter();
                log.info("Sending service description description to rSYBL");
                marshaller.marshal(cloudServiceXML, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/description").build();
                HttpPut putDeployment = new HttpPut(putDeploymentStructureURL);

                StringEntity entity = new StringEntity(sw.getBuffer().toString());

                entity.setContentType("application/xml");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Executing request " + putDeployment.getRequestLine());
                HttpResponse response = httpClient.execute(endpoint, putDeployment);
                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CompositionRulesConfiguration.class);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter sw = new StringWriter();
                log.info("Sending  updated composition rules");
                marshaller.marshal(compositionRulesConfiguration, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/compositionRules").build();
                HttpPut putDeployment = new HttpPut(putDeploymentStructureURL);

                StringEntity entity = new StringEntity(sw.getBuffer().toString());

                entity.setContentType("application/xml");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Executing request " + putDeployment.getRequestLine());
                HttpResponse response = httpClient.execute(endpoint, putDeployment);
                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/elasticityCapabilitiesEffects").build();
                HttpPut putDeployment = new HttpPut(putDeploymentStructureURL);

                String jsonEffectsDescription = capabilitiesToJSON(serviceTemplate);

                StringEntity entity = new StringEntity(jsonEffectsDescription);

                entity.setContentType("application/json");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Send updated Effects");
                log.info(effectsJSON);

                HttpResponse response = httpClient.execute(endpoint, putDeployment);

                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI prepareConfigURI = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/startControl").build();
            HttpPut prepareConfig = new HttpPut(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }

    }

    public void sendUpdatedConfigToRSYBL(CloudService serviceTemplate, CompositionRulesConfiguration compositionRulesConfiguration, String effectsJSON) {

        HttpHost endpoint = new HttpHost(rSYBL_BASE_IP, rSYBL_BASE_PORT);

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CompositionRulesConfiguration.class);

                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter sw = new StringWriter();
                log.info("Sending  updated composition rules");
                marshaller.marshal(compositionRulesConfiguration, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/compositionRules").build();
                HttpPost putDeployment = new HttpPost(putDeploymentStructureURL);

                StringEntity entity = new StringEntity(sw.getBuffer().toString());

                entity.setContentType("application/xml");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Executing request " + putDeployment.getRequestLine());
                HttpResponse response = httpClient.execute(endpoint, putDeployment);
                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/elasticityCapabilitiesEffects").build();
                HttpPost putDeployment = new HttpPost(putDeploymentStructureURL);

                StringEntity entity = new StringEntity(effectsJSON);

                entity.setContentType("application/json");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Send updated Effects");
                log.info(effectsJSON);

                HttpResponse response = httpClient.execute(endpoint, putDeployment);

                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CloudServiceXML.class);
                CloudServiceXML cloudServiceXML = toRSYBLRepresentation(serviceTemplate);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                StringWriter sw = new StringWriter();
                log.info("Sending updated service description to rSYBL");
                marshaller.marshal(cloudServiceXML, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/" + serviceTemplate.getId() + "/elasticityRequirements").build();
                HttpPost putDeployment = new HttpPost(putDeploymentStructureURL);

                StringEntity entity = new StringEntity(sw.getBuffer().toString());

                entity.setContentType("application/xml");
                entity.setChunked(true);

                putDeployment.setEntity(entity);

                log.info("Executing request " + putDeployment.getRequestLine());
                HttpResponse response = httpClient.execute(endpoint, putDeployment);
                HttpEntity resEntity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (response.getStatusLine().getStatusCode() == 200) {

                }
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println("Chunked?: " + resEntity.isChunked());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

    }
//
//    public CompositionRulesConfiguration loadDefaultMetricCompositionRules() {
//        CompositionRulesConfiguration compositionRulesConfiguration = null;
//        try {
//            JAXBContext a = JAXBContext.newInstance(CompositionRulesConfiguration.class);
//            Unmarshaller u = a.createUnmarshaller();
//
//            Object object = u.unmarshal(new FileReader(new File("./config/resources/compositionRules.xml")));
//            compositionRulesConfiguration = (CompositionRulesConfiguration) object;
//
//        } catch (JAXBException e) {
//            log.error(e.getStackTrace().toString());
//        } catch (FileNotFoundException ex) {
//            java.util.logging.Logger.getLogger(rSYBLInterraction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return compositionRulesConfiguration;
//    }

    public CompositionRulesConfiguration loadMetricCompositionRules(String serviceID, String path) {
        CompositionRulesConfiguration compositionRulesConfiguration = null;
        try {
            JAXBContext a = JAXBContext.newInstance(CompositionRulesConfiguration.class);
            Unmarshaller u = a.createUnmarshaller();

            Object object = u.unmarshal(new FileReader(new File(path)));
            compositionRulesConfiguration = (CompositionRulesConfiguration) object;

        } catch (JAXBException e) {
            log.error(e.getStackTrace().toString());
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(rSYBLInterraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        compositionRulesConfiguration.setTargetServiceID(serviceID);

        return compositionRulesConfiguration;
    }

//    public String loadDefaultJSONEffects() {
//        String json = "";
//        String line = "";
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader("./config/resources/effects.json"));
//            while ((line = reader.readLine()) != null) {
//                json += line;
//            }
//        } catch (FileNotFoundException ex) {
//            java.util.logging.Logger.getLogger(rSYBLInterraction.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(rSYBLInterraction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return json;
//    }
    public String loadJSONEffects(String path) {
        String json = "";
        String line = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            while ((line = reader.readLine()) != null) {
                json += line;
            }
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(rSYBLInterraction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(rSYBLInterraction.class.getName()).log(Level.SEVERE, null, ex);
        }

        return json;
    }

    public CloudServiceXML toRSYBLRepresentation(CloudService serviceTemplate) {
        CloudServiceXML cloudServiceXML = new CloudServiceXML();
        cloudServiceXML.setId(serviceTemplate.getId());

        //Capability ID 
        Map<String, ServiceUnit> capabilitiesPerUnit = new HashMap<>();
        Map<String, ServiceUnit> requirementsPerUnit = new HashMap<>();

        //used by rsybl to determine control and other dependencies
        //TODO:m switch to public static final  String[] relationshipTypes={"COMPOSITION_RELATIONSHIP","HOSTED_ON_RELATIONSHIP", "ASSOCIATED_AT_RUNTIME_RELATIONSHIP", "RUNS_ON", "MASTER_OF", "PEER_OF", "CONTROL","DATA","LOAD","INSTANTIATION","POLYNIMIAL_RELATIONSHIP"}
//        Map<String, String> toFromRelationships = new HashMap<>();
        //build map connecting capabilities to units
        for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {
            for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
                for (Capability c : serviceUnit.getCapabilities()) {
//                    if (capabilitiesPerUnit.containsKey(c.getId())) {
//                        capabilitiesPerUnit.get(c.getId()).add(serviceUnit);
//                    } else {
//                        ArrayList<ServiceUnit> sus = new ArrayList<>();
//                        sus.add(serviceUnit);
                    capabilitiesPerUnit.put(c.getId(), serviceUnit);
//                    }
                }

                for (Requirement requirement : serviceUnit.getRequirements()) {
                    requirementsPerUnit.put(requirement.getId(), serviceUnit);
                }
            }
        }

        Set<EntityRelationship> entityRelationships = serviceTemplate.getRelationships();

        for (EntityRelationship entityRelationship : entityRelationships) {
            if (entityRelationship.getFrom() instanceof Capability
                    && entityRelationship.getTo() instanceof Requirement) {
                Capability from = (Capability) entityRelationship.getFrom();
                Requirement to = (Requirement) entityRelationship.getTo();

                ServiceUnit fromUnit = capabilitiesPerUnit.get(from.getId());
                ServiceUnit toUnit = requirementsPerUnit.get(to.getId());
                if (fromUnit != null && toUnit != null) {
//                    toFromRelationships.put(fromUnit.getId(), toUnit.getId());
                } else {
                    log.warn("Relationship " + entityRelationship + " has no capabilities/requirements");
                }
            }
        }

        List<ServiceTopologyXML> serviceTopologyXMLs = new ArrayList<>();
        cloudServiceXML.setServiceTopologies(serviceTopologyXMLs);

        for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {

            ServiceTopologyXML serviceTopologyXML = new ServiceTopologyXML();
            serviceTopologyXML.setId(serviceTopology.getId());

            serviceTopologyXMLs.add(serviceTopologyXML);

            List<ServiceUnitXML> serviceUnitXMLs = new ArrayList<>();
            serviceTopologyXML.setServiceUnits(serviceUnitXMLs);
            SYBLAnnotationXML topologyAnnotationXML = new SYBLAnnotationXML();

            for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
                if (!serviceUnit.getType().equals(ServiceUnit.NodeType.Software.toString())) {
                    //only gather Software type nodes
                    continue;
                }
                ServiceUnitXML serviceUnitXML = new ServiceUnitXML();
                String serviceUnitID = serviceUnit.getId();
                serviceUnitXML.setId(serviceUnitID);

                serviceUnitXMLs.add(serviceUnitXML);

                SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

//
//                if (toFromRelationships.containsKey(serviceUnitID)) {
//                    RelationshipXML relationshipXML = new RelationshipXML();
//                    relationshipXML.setSource(serviceUnitID);
//                    relationshipXML.setTarget(toFromRelationships.get(serviceUnitID));
//                    relationshipXML.setType(serviceUnitID);
//
//                    serviceTopologyXML.addRelationship(relationshipXML);
//                }
                if (serviceUnit.hasConstraints()) {

                    String costraints = "";

                    for (Constraint constraint : serviceUnit.getConstraints()) {

                        costraints += constraint.getId() + ":CONSTRAINT " + constraint.getMetric().getName()
                                + " " + constraint.getOperator().toString() + " "
                                + constraint.getValue() + " " + constraint.getMetric().getUnit() + ";";

                    }

                    costraints = costraints.replaceAll("  ", " ");
                    annotationXML.setConstraints(annotationXML.getConstraints() + costraints.trim());

                }

                if (serviceUnit.hasStrategies()) {
                    String strategies = "";

                    for (Strategy strategy : serviceUnit.getStrategies()) {

                        String costraints = "";
                        for (Constraint constraint : strategy.getConstraints()) {

                            costraints += constraint.getMetric().getName() + " "
                                    + constraint.getOperator().toString()
                                    + " " + constraint.getValue() + " "
                                    + constraint.getMetric().getUnit() + " " + strategy.getOperator().toString() + " ";
                        }

                        if (costraints.lastIndexOf("AND") > 0) {
                            costraints = costraints.substring(0, costraints.lastIndexOf("AND")).trim();
                        }

                        costraints = costraints.trim();
                        strategies += strategy.getId() + ":STRATEGY CASE " + costraints + ":" + strategy.getCapability().getType() + ";";

                    }

                    strategies = strategies.replaceAll("  ", " ");

                    annotationXML.setStrategies(annotationXML.getStrategies() + strategies.trim());

                }

                if (!annotationXML.getConstraints().isEmpty() || !annotationXML.getStrategies().isEmpty()) {
                    serviceUnitXML.setXMLAnnotation(annotationXML);
                }

            }

            if (serviceTopology.hasConstraints()) {

                String costraints = "";

                for (Constraint constraint : serviceTopology.getConstraints()) {
                    costraints += constraint.getId() + ":CONSTRAINT " + constraint.getMetric().getName()
                            + " " + constraint.getOperator().toString() + " " + constraint.getValue() + " " + constraint.getMetric().getUnit() + ";";
                }

                costraints = costraints.replaceAll("  ", " ");
                topologyAnnotationXML.setConstraints(topologyAnnotationXML.getConstraints() + costraints.trim());

            }

            if (serviceTopology.hasStrategies()) {
                String strategies = "";

                for (Strategy strategy : serviceTopology.getStrategies()) {

                    String costraints = "";
                    for (Constraint constraint : strategy.getConstraints()) {
                        costraints += constraint.getMetric().getName() + " "
                                + constraint.getOperator().toString()
                                + " " + constraint.getValue() + " " + constraint.getMetric().getUnit() + " " + strategy.getOperator().toString() + " ";
                    }

                    //remove last operator in strategy
                    if (costraints.lastIndexOf("AND") > 0) {
                        costraints = costraints.substring(0, costraints.lastIndexOf("AND")).trim();
                    }

                    costraints = costraints.trim();
                    strategies += strategy.getId() + ":STRATEGY CASE " + costraints + ":" + strategy.getCapability().getType() + ";";

                }

                strategies = strategies.replaceAll("  ", " ");

                topologyAnnotationXML.setStrategies(topologyAnnotationXML.getStrategies() + strategies.trim());
            }

            if (!topologyAnnotationXML.getConstraints().isEmpty() || !topologyAnnotationXML.getStrategies().isEmpty()) {
                serviceTopologyXML.setXMLAnnotation(topologyAnnotationXML);
            }

        }

        return cloudServiceXML;

    }

    /**
     * With side effects, Directly enriches supplied deploymentDescription
     *
     * @param deploymentDescription
     * @param serviceTemplate
     * @return
     */
    public DeploymentDescription enrichWithElasticityCapabilities(DeploymentDescription deploymentDescription, CloudService serviceTemplate) {
        //get a Map of Deployment Units and a map of SoftwareUnits, and match capabilities
        Map<String, ServiceUnit> softwareUnits = new HashMap<>();

        for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {
            for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
                softwareUnits.put(serviceUnit.getId(), serviceUnit);
            }
        }

        for (DeploymentUnit deploymentUnit : deploymentDescription.getDeployments()) {
            if (softwareUnits.containsKey(deploymentUnit.getServiceUnitID())) {
                Set<ElasticityCapability> capabilities = softwareUnits.get(deploymentUnit.getServiceUnitID()).getElasticityCapabilities();
                for (ElasticityCapability capability : capabilities) {
                    at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.ElasticityCapability ec = new at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.ElasticityCapability();
                    String primitiveOps = "";
                    if (!capability.getPrimitiveOperations().isEmpty()) {
                        for (String op : capability.getPrimitiveOperations()) {
                            primitiveOps += ";" + op;
                        }
                        primitiveOps = primitiveOps.substring(1);
                    } else {
                        primitiveOps = capability.getType().toString();
                    }

                    ec.setPrimitiveOperations(primitiveOps);
//                    ec.setType(capability.getType());
                    ec.setName(capability.getType());
                    ec.setScript(capability.getEndpoint());
                    deploymentUnit.getElasticityCapabilities().add(ec);
                }
            }
        }

        return deploymentDescription;

    }

    /**
     *
     * @param deploymentDescription enriched with elasticity capabilities
     * @return
     */
    public String capabilitiesToJSON(CloudService cloudService) {

        class CapabilityDescription {

            String capabilityType;
            AbstractCloudEntity capabilityTarget;

            List<CapabilityEffect> capabilityEffects;

            public CapabilityDescription(String capabilityType, AbstractCloudEntity capabilityTarget, List<CapabilityEffect> capabilityEffects) {
                this.capabilityType = capabilityType;
                this.capabilityTarget = capabilityTarget;
                this.capabilityEffects = capabilityEffects;
            }

            @Override
            public String toString() {
                return "CapabilityDescription{" + "capabilityType=" + capabilityType + ", capabilityTarget=" + capabilityTarget.getId() + '}';
            }

        };

        Map<String, List<CapabilityDescription>> effects = new HashMap<>();

        //get capabilities
        List<CapabilityDescription> capabilities = new ArrayList<>();

        for (ElasticityCapability capability : cloudService.getElasticityCapabilities()) {
            CapabilityDescription serviceCDescription = new CapabilityDescription(capability.getType(), cloudService, capability.getCapabilityEffects());
            capabilities.add(serviceCDescription);
        }

        for (ServiceTopology serviceTopology : cloudService.getServiceTopologies()) {
            for (ElasticityCapability capability : serviceTopology.getElasticityCapabilities()) {
                CapabilityDescription serviceCDescription = new CapabilityDescription(capability.getType(), serviceTopology, capability.getCapabilityEffects());
                capabilities.add(serviceCDescription);
            }
            for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
                for (ElasticityCapability capability : serviceUnit.getElasticityCapabilities()) {
                    CapabilityDescription serviceCDescription = new CapabilityDescription(capability.getType(), serviceUnit, capability.getCapabilityEffects());
                    capabilities.add(serviceCDescription);
                }
            }

        }

        for (CapabilityDescription capability : capabilities) {
            if (effects.containsKey(capability.capabilityType)) {
                effects.get(capability.capabilityType).add(capability);
            } else {
                List<CapabilityDescription> ces = new ArrayList<>();
                ces.add(capability);
                effects.put(capability.capabilityType, ces);
            }
        }
        JSONObject effectSpecification = new JSONObject();

        for (String capabilityType : effects.keySet()) {

            JSONObject descriptionOFStuffForThisActionTYPE = new JSONObject();

            for (CapabilityDescription capabilityDescription : effects.get(capabilityType)) {

                JSONObject capabilityJSON = new JSONObject();
                capabilityJSON.put("targetUnit", capabilityDescription.capabilityTarget.getId());

//                JSONArray metricEffects = new JSONArray();
                JSONObject metricEffectsDescription = new JSONObject();
                for (CapabilityEffect capabilityEffect : capabilityDescription.capabilityEffects) {

                    JSONObject metricEffectJSON = new JSONObject();
                    for (MetricEffect metricEffect : capabilityEffect.getMetricEffects()) {

                        //processing
                        switch (metricEffect.getEffectType()) {
                            case ADD:
                                metricEffectJSON.put(metricEffect.getMetric().getName(), metricEffect.getEffectValue());
                                break;

                            case SUB:
                                metricEffectJSON.put(metricEffect.getMetric().getName(), -metricEffect.getEffectValue());
                                break;

                            case DIV:
                                metricEffectJSON.put(metricEffect.getMetric().getName(), metricEffect.getEffectType().toString() + metricEffect.getEffectValue());
                                break;

                            case MUL:
                                metricEffectJSON.put(metricEffect.getMetric().getName(), metricEffect.getEffectType().toString() + metricEffect.getEffectValue());
                                break;

                        }

                    }

                    metricEffectsDescription.put(capabilityEffect.getTarget().getId(), metricEffectJSON);
//                    metricEffects.add(metricEffectsDescription);

                }

                capabilityJSON.put("effects", metricEffectsDescription);

                descriptionOFStuffForThisActionTYPE.put(capabilityType + "EffectFor" + capabilityDescription.capabilityTarget.getId(), capabilityJSON);

            }
            effectSpecification.put(capabilityType, descriptionOFStuffForThisActionTYPE);
        }

        return effectSpecification.toString();

    }

    public void setIp(String rSYBL_BASE_IP) {
        this.rSYBL_BASE_IP = rSYBL_BASE_IP;
    }

    public void setPort(Integer rSYBL_BASE_PORT) {
        this.rSYBL_BASE_PORT = rSYBL_BASE_PORT;
    }

    public void setBaseURI(String rSYBL_BASE_URL) {
        this.rSYBL_BASE_URL = rSYBL_BASE_URL;
    }

}
