package at.ac.tuwien.dsg.comot.api;

import at.ac.tuwien.dsg.comot.model.*;
import org.oasis.tosca.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author omoser
 */
public class ToscaDescriptionBuilderImpl implements ToscaDescriptionBuilder {

    private static final Logger log = LoggerFactory.getLogger(ToscaDescriptionBuilderImpl.class);

    Map<String, TExtensibleElements> context = new HashMap<>();

    Map<EntityRelationship, TTopologyTemplate> unresolvedRelationships = new HashMap<>();

    @Override
    public TDefinitions buildToscaDefinitions(CloudApplication application) throws Exception {
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
            List<TPolicy> tPolicies = new ArrayList<>();
            if (serviceTemplate.hasConstraints() || serviceTemplate.hasStrategies()) {
                tPolicies.addAll(getConstraintPolicies(serviceTemplate));
                tPolicies.addAll(getStrategyPolicies(serviceTemplate));
                boundaryDefinitions.setPolicies(new TBoundaryDefinitions.Policies().withPolicy(tPolicies));
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
                        .withMaxInstances(String.valueOf(node.getMinInstances()))
                        .withCapabilities(getCapabilities(node))
                        .withRequirements(getRequirements(node))
                        .withPolicies(getPolicies(node))
                        .withProperties(getProperties(node));

                context.put(tNodeTemplate.getId(), tNodeTemplate);
                tNodeTemplates.add(tNodeTemplate);
            }

            List<TEntityTemplate> entityTemplates = new ArrayList<>();
            entityTemplates.addAll(relationshipTemplates);
            entityTemplates.addAll(tNodeTemplates);
            tTopologyTemplate.withNodeTemplateOrRelationshipTemplate(entityTemplates);
            tServiceTemplate.setTopologyTemplate(tTopologyTemplate);
        }


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
            }
            return null; // todo might be better to throw exception
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

        return new TEntityTemplate.Properties();
    }

    // todo implement
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
            tPolicies.add(tPolicy);
        }
    }

    private void addConstraintsToNodeTemplate(ServiceNode node, List<TPolicy> tPolicies) {
        for (Constraint constraint : node.getConstraints()) {
            TPolicy tPolicy = new TPolicy()
                    .withPolicyType(new QName(constraint.getType()))
                    .withName(constraint.render());
            tPolicies.add(tPolicy);
        }
    }

    // todo implement
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
