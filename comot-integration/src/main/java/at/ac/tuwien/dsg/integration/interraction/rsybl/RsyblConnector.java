/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.integration.interraction.rsybl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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

import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
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


public class RsyblConnector {

    private static final Logger log = LoggerFactory.getLogger(RsyblConnector.class);

    private String rSYBL_BASE_IP = "128.130.172.214";
    private Integer rSYBL_BASE_PORT = 8081;
    private String rSYBL_BASE_URL = "/rsybl/restWS";

    public void sendInitialConfigToRSYBL(CloudService serviceTemplate, DeploymentDescription deploymentDescription, CompositionRulesConfiguration compositionRulesConfiguration, String effectsJSON) {

        deploymentDescription = enrichWithElasticityCapabilities(deploymentDescription, serviceTemplate);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpHost endpoint = new HttpHost(rSYBL_BASE_IP, rSYBL_BASE_PORT);

        {
            URI prepareConfigURI = UriBuilder.fromPath(rSYBL_BASE_URL + "/prepareControl").build();
            HttpPut prepareConfig = new HttpPut(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        {

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(DeploymentDescription.class);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter sw = new StringWriter();

                log.info("Sending deployment description to rsybl");
                marshaller.marshal(deploymentDescription, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/serviceDeployment").build();
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

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CloudServiceXML.class);
                CloudServiceXML cloudServiceXML = toRSYBLRepresentation(serviceTemplate);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                StringWriter sw = new StringWriter();
                log.info("Sending service description description to rsybl");
                marshaller.marshal(cloudServiceXML, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/serviceDescription").build();
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

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CompositionRulesConfiguration.class);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter sw = new StringWriter();
                log.info("Sending  updated composition rules");
                marshaller.marshal(compositionRulesConfiguration, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/metricsCompositionRules").build();
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

            try {

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/elasticityCapabilitiesEffects").build();
                HttpPut putDeployment = new HttpPut(putDeploymentStructureURL);

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
            URI prepareConfigURI = UriBuilder.fromPath(rSYBL_BASE_URL + "/startControl").build();
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
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpHost endpoint = new HttpHost(rSYBL_BASE_IP, rSYBL_BASE_PORT);

        {

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CompositionRulesConfiguration.class);

                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter sw = new StringWriter();
                log.info("Sending  updated composition rules");
                marshaller.marshal(compositionRulesConfiguration, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/replaceCompositionRulesFromCurrentCloudService").build();
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

            try {

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/replaceEffectsForCurrentCloudService").build();
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

            try {

                JAXBContext jAXBContext = JAXBContext.newInstance(CloudServiceXML.class);
                CloudServiceXML cloudServiceXML = toRSYBLRepresentation(serviceTemplate);
                Marshaller marshaller = jAXBContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                StringWriter sw = new StringWriter();
                log.info("Sending updated service description to rsybl");
                marshaller.marshal(cloudServiceXML, sw);
                log.info(sw.toString());

                URI putDeploymentStructureURL = UriBuilder.fromPath(rSYBL_BASE_URL + "/replaceCloudServiceRequirementsFromCurrentCloudService").build();
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

    public CompositionRulesConfiguration loadMetricCompositionRules(String path) {
        CompositionRulesConfiguration compositionRulesConfiguration = null;
        try {
            JAXBContext a = JAXBContext.newInstance(CompositionRulesConfiguration.class);
            Unmarshaller u = a.createUnmarshaller();

            Object object = u.unmarshal(new FileReader(new File(path)));
            compositionRulesConfiguration = (CompositionRulesConfiguration) object;

        } catch (JAXBException e) {
            log.error(e.getStackTrace().toString());
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(RsyblConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

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
            java.util.logging.Logger.getLogger(RsyblConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RsyblConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return json;
    }

    public CloudServiceXML toRSYBLRepresentation(CloudService serviceTemplate) {
        CloudServiceXML cloudServiceXML = new CloudServiceXML();
        cloudServiceXML.setId(serviceTemplate.getId());

        //Capability ID 
        Map<String, ServiceUnit> capabilitiesPerUnit = new HashMap<>();
        Map<String, ServiceUnit> requirementsPerUnit = new HashMap<>();

        Map<String, String> toFromRelationships = new HashMap<>();

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
                    toFromRelationships.put(fromUnit.getId(), toUnit.getId());
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

            for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
                if (!serviceUnit.getType().equals(ServiceUnit.NodeType.Software.toString())) {
                    //only gather Software type nodes
                    continue;
                }
                ServiceUnitXML serviceUnitXML = new ServiceUnitXML();
                String serviceUnitID = serviceUnit.getId();
                serviceUnitXML.setId(serviceUnitID);

                serviceUnitXMLs.add(serviceUnitXML);

                if (toFromRelationships.containsKey(serviceUnitID)) {
                    RelationshipXML relationshipXML = new RelationshipXML();
                    /*relationshipXML.setMaster(serviceUnitID);
                    relationshipXML.setSlave(toFromRelationships.get(serviceUnitID));

                    serviceTopologyXML.setRelationship(relationshipXML);*/
                }

                if (serviceUnit.hasConstraints()) {

                    String costraints = "";

                    for (Constraint constraint : serviceUnit.getConstraints()) {

                        costraints += constraint.getId() + ":CONSTRAINT " + constraint.getMetric().getName()
                                + " " + constraint.getOperator().toString() + " "
                                + constraint.getValue() + " " + constraint.getMetric().getUnit() + ";";

                    }
                    SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

                    costraints = costraints.replaceAll("  ", " ");
                    annotationXML.setConstraints(costraints.trim());

                    serviceUnitXML.setXMLAnnotation(annotationXML);
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
                        strategies = strategy.getId() + ":STRATEGY CASE " + costraints + ":" + strategy.getAction().toString() + ";";

                    }
                    SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();
                    strategies = strategies.replaceAll("  ", " ");
                    annotationXML.setStrategies(strategies);

                    serviceUnitXML.setXMLAnnotation(annotationXML);
                }

            }

            if (serviceTopology.hasConstraints()) {

                String costraints = "";

                for (Constraint constraint : serviceTopology.getConstraints()) {
                    costraints += constraint.getId() + ":CONSTRAINT " + constraint.getMetric().getName()
                            + " " + constraint.getOperator().toString() + " " + constraint.getValue() + " " + constraint.getMetric().getUnit() + ";";
                }
                SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

                costraints = costraints.replaceAll("  ", " ");
                annotationXML.setConstraints(costraints.trim());

                serviceTopologyXML.setXMLAnnotation(annotationXML);
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
                    strategies += strategy.getId() + ":STRATEGY CASE " + costraints + ":" + strategy.getAction().toString() + ";";

                }
                SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

                strategies = strategies.replaceAll("  ", " ");

                annotationXML.setStrategies(strategies.trim());

                serviceTopologyXML.setXMLAnnotation(annotationXML);
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
                    ec.setType(capability.getType());
                    ec.setScript(capability.getEndpoint());
                    deploymentUnit.getElasticityCapabilities().add(ec);
                }
            }
        }

        return deploymentDescription;

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
