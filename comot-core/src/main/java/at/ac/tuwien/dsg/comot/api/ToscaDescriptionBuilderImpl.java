package at.ac.tuwien.dsg.comot.api;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.comot.common.model.*;
import com.google.common.base.Joiner;
import org.oasis.tosca.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * @author omoser
 */
@Component
public class ToscaDescriptionBuilderImpl implements ToscaDescriptionBuilder {

    private static final Logger log = LoggerFactory.getLogger(ToscaDescriptionBuilderImpl.class);

    public static final String DEFAULT_ARTIFACT_TYPE_NAMESPACE_URI = "http://void.org";

    public static final String DEFAULT_ARTIFACT_TYPE_PREFIX = "salsa";

    public static final String DEFAULT_TOSCA_SCHEMA_FILENAME = "TOSCA-v1.0.xsd";

    private Map<String, TExtensibleElements> context = new HashMap<>();

    private Map<EntityRelationship, TTopologyTemplate> unresolvedRelationships = new HashMap<>();

    private boolean validatingMarshaller;

    private String toscaSchemaFilename = DEFAULT_TOSCA_SCHEMA_FILENAME;

    @Override
    public TDefinitions buildToscaDefinitions(CloudApplication application) throws ToscaDescriptionBuilderException {
        if (log.isTraceEnabled()) {
            log.trace("Building TOSCA definitions for application: {}", application);
        }

        Definitions definitions = buildTDefinitions(application);
        List<TExtensibleElements> tServiceTemplates = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();
        for (ServiceTemplate serviceTemplate : application.getServiceTemplates()) {
            TServiceTemplate tServiceTemplate = getTServiceTemplate(serviceTemplate.getId());
            context.put(tServiceTemplate.getId(), tServiceTemplate);

            TBoundaryDefinitions boundaryDefinitions = new TBoundaryDefinitions();
            tServiceTemplate.setBoundaryDefinitions(boundaryDefinitions);

            if (serviceTemplate.hasConstraints() || serviceTemplate.hasStrategies()) {
                addPolicies(serviceTemplate, boundaryDefinitions);
            }

            if (serviceTemplate.hasRequirements()) {
                boundaryDefinitions.setRequirements(getRequirements(serviceTemplate));
            }

            if (serviceTemplate.hasCapabilities()) {
                boundaryDefinitions.setCapabilities(getCapabilities(serviceTemplate));
            }

            tServiceTemplates.add(tServiceTemplate);

            ServiceTopology topology = serviceTemplate.getServiceTopology();
            TTopologyTemplate tTopologyTemplate = getTTopologyTemplate(topology.getId());

            List<TRelationshipTemplate> relationshipTemplates = new ArrayList<>();
            if (topology.hasRelationships()) {
                relationshipTemplates.addAll(extractRelationships(topology, tTopologyTemplate));
            }

            List<TNodeTemplate> tNodeTemplates = new ArrayList<>();
            for (ServiceNode node : topology.getServiceNodes()) {
                TNodeTemplate tNodeTemplate = new TNodeTemplate()
                        .withId(node.getId())
                        .withName(node.getName())
                        .withType(new QName(node.getType()))
                        .withMinInstances(node.getMinInstances())
                        .withMaxInstances(String.valueOf(node.getMaxInstances()))
                        .withCapabilities(getCapabilities(node))
                        .withRequirements(getRequirements(node))
                        .withPolicies(getPolicies(node))
                        .withProperties(getProperties(node));

                handleDeploymentArtifacts(node, tNodeTemplate, definitions);
                context.put(tNodeTemplate.getId(), tNodeTemplate);
                tNodeTemplates.add(tNodeTemplate);
            }

            List<TEntityTemplate> entityTemplates = new ArrayList<>();
            entityTemplates.addAll(relationshipTemplates);
            entityTemplates.addAll(tNodeTemplates);
            tTopologyTemplate.withNodeTemplateOrRelationshipTemplate(entityTemplates);
            tServiceTemplate.setTopologyTemplate(tTopologyTemplate);
        }

        // add deployment artifacts


        // resolve unresolved relationships, throw exception if there are still unresolved entities
        if (!unresolvedRelationships.isEmpty()) {
            for (Map.Entry<EntityRelationship, TTopologyTemplate> unresolvedRelationship : unresolvedRelationships.entrySet()) {
                EntityRelationship entityRelationship = unresolvedRelationship.getKey();
                TTopologyTemplate tTopologyTemplate = unresolvedRelationship.getValue();
                TRelationshipTemplate tRelationshipTemplate = buildTRelationshipTemplate(entityRelationship, true);
                tTopologyTemplate.getNodeTemplateOrRelationshipTemplate().add(tRelationshipTemplate);
            }

        }

        return definitions;

    }

    @Override
    public String toXml(CloudApplication application) throws ToscaDescriptionBuilderException {
        try {
            TDefinitions tDefinitions = new ToscaDescriptionBuilderImpl().buildToscaDefinitions(application);
            JAXBContext jaxbContext = JAXBContext.newInstance(TDefinitions.class, SalsaMappingProperties.class, BundleConfig.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter writer = new StringWriter();
            if (validatingMarshaller) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = sf.newSchema(new ClassPathResource(toscaSchemaFilename).getFile());
                marshaller.setSchema(schema);
                marshaller.setEventHandler(new DefaultValidationEventHandler());
            }

            // todo handle target namespace to allow validation
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(tDefinitions, writer);
            return writer.toString();
        } catch (JAXBException e) {
            log.error(Markers.API, "Exception during marshalling of CloudApplication", e);
            throw new ToscaDescriptionBuilderException("CloudApplication '"
                    + application.getName() + "' could not be marshalled", e);
        } catch (SAXException e) {
            log.error(Markers.API, "Cannot load TOSCA schema definition from " + toscaSchemaFilename, e);
            throw new ToscaDescriptionBuilderException("Unable to load TOSCA schema definition", e);
        } catch (IOException e) {
            log.error("Cannot find TOSCA schema definition on classpath", e);
            throw new ToscaDescriptionBuilderException("Unable to find TOSCA schema defintion on classpath", e);
        }
    }

    @Override
    public void setValidating(boolean validating) {
        this.validatingMarshaller = validating;
    }

    public void setValidatingMarshaller(boolean validatingMarshaller) {
        this.validatingMarshaller = validatingMarshaller;
    }

    private void handleDeploymentArtifacts(ServiceNode node, TNodeTemplate tNodeTemplate, Definitions definitions) {
        for (ArtifactTemplate artifact : node.getDeploymentArtifacts()) {

            TArtifactTemplate tArtifactTemplate = new TArtifactTemplate()
                    .withId(artifact.getId())
                    .withType(new QName(artifact.getType()));


            definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(tArtifactTemplate);

            TDeploymentArtifact tDeploymentArtifact = new TDeploymentArtifact()
                    .withName(artifact.getId())
                    .withArtifactType(buildArtifactTypeQname(artifact)) // todo what to do here?
                    .withArtifactRef(new QName(tArtifactTemplate.getId()));

            TDeploymentArtifacts deploymentArtifacts = new TDeploymentArtifacts();
            tNodeTemplate.setDeploymentArtifacts(deploymentArtifacts);
            deploymentArtifacts.getDeploymentArtifact().add(tDeploymentArtifact);

            List<TArtifactReference> tArtifactReferences = new ArrayList<>();
            for (ArtifactReference reference : artifact.getArtifactReferences()) {
                tArtifactReferences.add(new TArtifactReference().withReference(reference.getUri()));
            }

            // add properties
            TEntityTemplate.Properties properties = new TEntityTemplate.Properties();
            properties.withAny(artifact.getBundleConfig());
            tArtifactTemplate.setProperties(properties);

            tArtifactTemplate.setArtifactReferences(new TArtifactTemplate.ArtifactReferences()
                            .withArtifactReference(tArtifactReferences)
            );

        }
    }

    private QName buildArtifactTypeQname(ArtifactTemplate artifact) {
        return new QName(DEFAULT_ARTIFACT_TYPE_NAMESPACE_URI, artifact.getType(), DEFAULT_ARTIFACT_TYPE_PREFIX);
    }

    private void addPolicies(ServiceTemplate serviceTemplate, TBoundaryDefinitions boundaryDefinitions) {
        List<TPolicy> tPolicies = new ArrayList<>();
        tPolicies.addAll(getConstraintPolicies(serviceTemplate));
        tPolicies.addAll(getStrategyPolicies(serviceTemplate));
        boundaryDefinitions.setPolicies(new TBoundaryDefinitions.Policies().withPolicy(tPolicies));
    }

    // todo implement getCapabilities(ServiceTemplate)
    private TBoundaryDefinitions.Capabilities getCapabilities(ServiceTemplate serviceTemplate) {
        return new TBoundaryDefinitions.Capabilities();
    }

    // todo implement getRequirements(ServiceTemplate)
    private TBoundaryDefinitions.Requirements getRequirements(ServiceTemplate serviceTemplate) {
        return new TBoundaryDefinitions.Requirements();
    }


    private TRelationshipTemplate buildTRelationshipTemplate(EntityRelationship relationship, boolean throwOnMissingRelationship) {
        CloudEntity from = relationship.getFrom();
        CloudEntity to = relationship.getTo();

        // check if we already have a reference to source and target
        TExtensibleElements source = context.get(from.getId());
        TExtensibleElements target = context.get(to.getId());

        if (source != null && target != null) {
            return new TRelationshipTemplate()
                    .withType(new QName(relationship.getType()))
                    .withId(relationship.getId())
                    .withSourceElement(new TRelationshipTemplate.SourceElement().withRef(source))
                    .withTargetElement(new TRelationshipTemplate.TargetElement().withRef(target));
        } else {
            log.warn("Either source or target of EntiyRelationship {} is not available: source: {} target: {}",
                    relationship, source, target);

            if (throwOnMissingRelationship) {
                throw new IllegalStateException("Cannot resolve source or target element for relationship");
            } else {
                return null;
            }
        }
    }

    private List<TRelationshipTemplate> extractRelationships(ServiceTopology topology, TTopologyTemplate tTopologyTemplate) {
        List<TRelationshipTemplate> relationshipTemplates = new ArrayList<>();
        for (EntityRelationship relationship : topology.getRelationships()) {
            TRelationshipTemplate template = buildTRelationshipTemplate(relationship, false);
            if (template != null) {
                relationshipTemplates.add(template);
            } else {
                // mark unresolved
                unresolvedRelationships.put(relationship, tTopologyTemplate);
            }
        }

        return relationshipTemplates;
    }

    private TTopologyTemplate getTTopologyTemplate(String id) {
        return context.get(id) != null ? (TTopologyTemplate) context.get(id) : new TTopologyTemplate();
    }

    private TServiceTemplate getTServiceTemplate(String id) {
        return context.get(id) != null ? (TServiceTemplate) context.get(id) : new TServiceTemplate().withId(id);
    }


    // todo implement
    private TEntityTemplate.Properties getProperties(ServiceNode node) {
        TEntityTemplate.Properties properties = new TEntityTemplate.Properties();
        if (node instanceof OperatingSystemNode) {
            OperatingSystemNode osNode = (OperatingSystemNode) node;
            OperatingSystemSpecification specification = osNode.getSpecification();
            SalsaMappingProperties salsaMappingProperties = new SalsaMappingProperties();
            Map<String, String> osProperties = new HashMap<>();
            osProperties.put("instanceType", specification.getInstanceType());
            osProperties.put("provider", specification.getProvider());
            osProperties.put("baseImage", specification.getBaseImage());
            osProperties.put("packages", joinOsSpecificationPackages(specification));
            salsaMappingProperties.put("os", osProperties);
            properties.withAny(salsaMappingProperties);
        }

        return properties;
    }

    private String joinOsSpecificationPackages(OperatingSystemSpecification specification) {
        return Joiner.on(",").join(specification.getPackages());
    }


    private TNodeTemplate.Policies getPolicies(ServiceNode node) {
        List<TPolicy> tPolicies = new ArrayList<>();
        addConstraintsToNodeTemplate(node, tPolicies);
        addStrategiesToNodeTemplate(node, tPolicies);
        return new TNodeTemplate.Policies().withPolicy(tPolicies);
    }

    private void addStrategiesToNodeTemplate(ServiceNode node, List<TPolicy> tPolicies) {
        for (Strategy strategy : node.getStrategies()) {
            TPolicy tPolicy = new TPolicy()
                    .withPolicyType(new QName(strategy.getStrategyConstraintType().toString()))
                    .withName(strategy.render());

            // todo no ID to put strategy into context map
            tPolicies.add(tPolicy);
        }
    }

    private void addConstraintsToNodeTemplate(ServiceNode node, List<TPolicy> tPolicies) {
        for (Constraint constraint : node.getConstraints()) {
            TPolicy tPolicy = new TPolicy()
                    .withPolicyType(new QName(constraint.getType()))
                    .withName(constraint.render());

            // todo no ID to put constraint into context map
            tPolicies.add(tPolicy);
        }
    }

    private TNodeTemplate.Requirements getRequirements(ServiceNode node) {
        Collection<TRequirement> requirements = new ArrayList<>();
        for (Requirement requirement : node.getRequirements()) {
            TRequirement tRequirement = new TRequirement()
                    .withId(requirement.getId())
                    .withName(requirement.getName())
                    .withType(new QName(requirement.getType()));

            context.put(tRequirement.getId(), tRequirement);
            requirements.add(tRequirement);
        }

        return new TNodeTemplate.Requirements().withRequirement(requirements);
    }

    private TNodeTemplate.Capabilities getCapabilities(ServiceNode node) {
        Collection<TCapability> capabilities = new ArrayList<>();
        for (Capability capability : node.getCapabilities()) {
            TCapability tCapability = new TCapability()
                    .withName(capability.getName())
                    .withId(capability.getId())
                    .withType(new QName(capability.getType()));

            context.put(tCapability.getId(), tCapability);
            capabilities.add(tCapability);
        }

        return new TNodeTemplate.Capabilities().withCapability(capabilities);
    }


    private List<TPolicy> getConstraintPolicies(ServiceTemplate serviceTemplate) {
        List<TPolicy> policies = new ArrayList<>();
        for (Constraint constraint : serviceTemplate.getConstraints()) {
            TPolicy tPolicy = new TPolicy()
                    .withName(constraint.render())
                    .withPolicyType(new QName(constraint.getType()));

            // todo there is no ID that we can use to put the constraint into the context map
            policies.add(tPolicy);
        }

        return policies;
    }


    private List<TPolicy> getStrategyPolicies(ServiceTemplate serviceTemplate) {
        List<TPolicy> policies = new ArrayList<>();
        for (Strategy strategy : serviceTemplate.getStrategies()) {
            TPolicy tPolicy = new TPolicy()
                    .withName(strategy.render())
                    .withPolicyType(new QName(strategy.getType()));

            // todo there is no ID that we can use to put the strategy into the context map
            policies.add(tPolicy);
        }

        return policies;
    }

    private Definitions buildTDefinitions(CloudApplication application) {
        Definitions definitions = new Definitions();
        definitions.setId(application.getId());
        definitions.setName(application.getName());
        return definitions;
    }


}
