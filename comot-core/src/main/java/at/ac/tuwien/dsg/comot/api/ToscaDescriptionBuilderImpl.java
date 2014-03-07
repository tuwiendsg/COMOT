package at.ac.tuwien.dsg.comot.api;

import at.ac.tuwien.dsg.comot.model.*;
import org.oasis.tosca.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author omoser
 */
public class ToscaDescriptionBuilderImpl implements ToscaDescriptionBuilder {

    private static final Logger log = LoggerFactory.getLogger(ToscaDescriptionBuilderImpl.class);

    @Override
    public TDefinitions buildToscaDefinitions(CloudApplication application) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("Building TOSCA definitions for application: {}", application);
        }

        Definitions definitions= buildTDefinitions(application);
        List<TExtensibleElements> tServiceTemplates = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();
        for (ServiceTemplate serviceTemplate : application.getServiceTemplates()) {
            TServiceTemplate tServiceTemplate = new TServiceTemplate();
            if (serviceTemplate.hasConstraints()) {
                tServiceTemplate.setBoundaryDefinitions(new TBoundaryDefinitions().withPolicies(getPolicies(serviceTemplate)));
            }

            if (serviceTemplate.hasRequirements()) {
                // todo add requirements
            }

            if (serviceTemplate.hasCapabilities()) {
                // todo add capabilities
            }

            if (serviceTemplate.hasStrategies()) {
                // todo add strategies
            }

            tServiceTemplates.add(tServiceTemplate);

            TTopologyTemplate tTopologyTemplate = new TTopologyTemplate();
            ServiceTopology topology = serviceTemplate.getServiceTopology();



            List<TRelationshipTemplate> relationshipTemplates = new ArrayList<>();
            if (topology.hasRelationships()) {
                for (EntityRelationship relationship : topology.getRelationships()) {
                    TRelationshipTemplate tRelationshipTemplate = new TRelationshipTemplate();
                    TRelationshipTemplate.SourceElement source = findEntityReference(relationship.getId(), application);
                    tRelationshipTemplate.withSourceElement(source);
                }

            }

            List<TNodeTemplate> tNodeTemplates = new ArrayList<>();
            for (ServiceNode node : topology.getServiceNodes()) {
                tNodeTemplates.add(new TNodeTemplate()
                        .withId(node.getId())
                        .withName(node.getName())
                        .withType(new QName(node.getType()))
                        .withMinInstances(node.getMinInstances())
                        .withMaxInstances(String.valueOf(node.getMinInstances()))
                        .withCapabilities(getCapabilities(node))
                        .withRequirements(getRequirements(node))
                        .withPolicies(getPolicies(node))
                        .withProperties(getProperties(node)));
            }

            List<TEntityTemplate> entityTemplates = new ArrayList<>();
            entityTemplates.addAll(relationshipTemplates);
            entityTemplates.addAll(tNodeTemplates);
            tTopologyTemplate.withNodeTemplateOrRelationshipTemplate(entityTemplates);
            tServiceTemplate.setTopologyTemplate(tTopologyTemplate);

        }

        return definitions;

    }


    // todo implement
    private TEntityTemplate.Properties getProperties(ServiceNode node) {
        return new TEntityTemplate.Properties();
    }

    // todo implement
    private TNodeTemplate.Policies getPolicies(ServiceNode node) {
        return new TNodeTemplate.Policies();
    }

    // todo implement
    private TNodeTemplate.Requirements getRequirements(ServiceNode node) {
        return new TNodeTemplate.Requirements();
    }

    private TNodeTemplate.Capabilities getCapabilities(ServiceNode node) {
        Collection<TCapability> capabilities = new ArrayList<>();
        for (Capability capability : node.getCapabilities()) {
            capabilities.add(new TCapability()
                    .withName(capability.getName())
                    .withId(capability.getId())
                    .withType(new QName(capability.getType()))
            );
        }

        return new TNodeTemplate.Capabilities().withCapability(capabilities);
    }

    private TRelationshipTemplate.SourceElement findEntityReference(String id, CloudApplication application) {
        return new TRelationshipTemplate.SourceElement();
    }


    private TBoundaryDefinitions.Policies getPolicies(ServiceTemplate serviceTemplate) {
        List<TPolicy> policies = new ArrayList<>();
        for (Constraint constraint : serviceTemplate.getConstraints()) {
            policies.add(new TPolicy()
                    .withName(constraint.render())
                    .withPolicyType(new QName(constraint.getConstraintType().toString()))
            );
        }

        return new TBoundaryDefinitions.Policies().withPolicy(policies);
    }

    private Definitions buildTDefinitions(CloudApplication application) {
        Definitions definitions = new Definitions();
        definitions.setId(application.getId());
        definitions.setName(application.getName());
        return definitions;
    }
}
