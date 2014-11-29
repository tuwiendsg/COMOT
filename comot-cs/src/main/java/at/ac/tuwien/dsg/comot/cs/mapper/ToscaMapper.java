package at.ac.tuwien.dsg.comot.cs.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.oasis.tosca.Definitions;
import org.oasis.tosca.TArtifactTemplate;
import org.oasis.tosca.TEntityTemplate;
import org.oasis.tosca.TExtensibleElements;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TRelationshipTemplate;
import org.oasis.tosca.TServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.common.model.logic.RelationshipResolver;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.ToscaOrika;

@Component
public class ToscaMapper {

	protected final Logger log = LoggerFactory.getLogger(ToscaMapper.class);

	@Autowired
	protected ToscaOrika mapper;

	public Definitions extractTosca(CloudService cloudService) throws JAXBException {

		Definitions definition = mapper.get().map(cloudService, Definitions.class);
		Navigator navigator = new Navigator(cloudService);

		log.trace("Mapping by orica: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

		TArtifactTemplate tArtifact;

		// inject TArtifactTemplates
		for (StackNode unit : navigator.getAllNodes()) {
			for (ArtifactTemplate artifact : unit.getDeploymentArtifacts()) {

				tArtifact = mapper.get().map(artifact, TArtifactTemplate.class);
				definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(tArtifact);
			}
		}

		String sourceTopoId;
		String targetTopoId;

		for (TExtensibleElements element : definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TServiceTemplate) {
				TServiceTemplate topology = (TServiceTemplate) element;

				// inject Relationships
				for (TRelationshipTemplate oneRel : createTRelationshps(cloudService.getRelationships())) {

					log.trace("TRelationshipTemplate id={}, type={}, from={}, to={} ",
							oneRel.getId(), oneRel.getType(), oneRel.getSourceElement().getRef(), oneRel
									.getTargetElement().getRef());

					sourceTopoId = navigator.getParentTopologyFor(
							((TEntityTemplate) oneRel.getSourceElement().getRef()).getId()).getId();
					targetTopoId = navigator.getParentTopologyFor(
							((TEntityTemplate) oneRel.getTargetElement().getRef()).getId()).getId();

					log.trace("Inserted relationship id={}, from={}, to={}", oneRel.getId(), sourceTopoId, targetTopoId);

					if (sourceTopoId.equals(topology.getId()) || targetTopoId.equals(topology.getId())) {
						topology.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate().add(oneRel);
					}
				}

			}
		}

		log.debug("Final mapping: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

		return definition;
	}

	public CloudService createModel(Definitions definitions) {

		ArtifactTemplate artifact;
		String from;
		String to;
		ServiceUnit unit;

		CloudService cloudService = mapper.get().map(definitions, CloudService.class);

		// if(cloudService.getId().equals("ElasticIoTPlatform"))
		// log.trace("Mapping by orika: {}", Utils.asJsonString(cloudService));

		Navigator navigator = new Navigator(cloudService);

		for (TExtensibleElements element : definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			// inject ArtifactTemplates
			if (element instanceof TArtifactTemplate) {

				artifact = mapper.get().map((TArtifactTemplate) element, ArtifactTemplate.class);

				for (StackNode node : navigator.getAllNodes()) {
					for (ArtifactTemplate currentArt : node.getDeploymentArtifacts()) {
						if (currentArt.getId().equals(artifact.getId())) {
							currentArt.setArtifactReferences(artifact.getArtifactReferences());
						}
					}
				}

				// inject relationships
			} else if (element instanceof TServiceTemplate) {
				for (TExtensibleElements element2 : ((TServiceTemplate) element).getTopologyTemplate()
						.getNodeTemplateOrRelationshipTemplate()) {
					if (element2 instanceof TRelationshipTemplate) {
						TRelationshipTemplate rel = (TRelationshipTemplate) element2;

						if (!cloudService.containsRelationship(rel.getId())) {
							from = ((TEntityTemplate) rel.getSourceElement().getRef()).getId();
							to = ((TEntityTemplate) rel.getTargetElement().getRef()).getId();

							cloudService.addEntityRelationship(new EntityRelationship(rel.getId(),
									RelationshipType.fromString(rel.getType().getLocalPart()), from, to));
						}
					}
				}
			}
		}

		navigator = new Navigator(cloudService); // recreate with new elements
		RelationshipResolver resolver = new RelationshipResolver(cloudService);

		// remove and add ServiceUnits
		for (StackNode node : navigator.getAllNodes()) {
			unit = navigator.getServiceUnit(node.getId());

			if (resolver.isServiceUnit(node)) {

				if (unit == null) {
					unit = new ServiceUnit(node);
					navigator.getParentTopologyFor(node.getId()).getServiceUnits().add(unit);
				}
			} else {
				if (unit != null) {
					navigator.getParentTopologyFor(node.getId()).getServiceUnits().remove(unit);
				}
			}
		}

		log.debug("Final mapping: {}", Utils.asJsonString(cloudService));

		return cloudService;
	}

	protected List<TRelationshipTemplate> createTRelationshps(List<EntityRelationship> relationships) {

		List<TRelationshipTemplate> relTemplates = new ArrayList<>();

		for (EntityRelationship relationship : relationships) {

			// create mock object for referencing
			TEntityTemplate source = new TNodeTemplate().withId(relationship.getFrom());
			TEntityTemplate target = new TNodeTemplate().withId(relationship.getTo());

			relTemplates.add(new TRelationshipTemplate()
					.withType(mapper.get().map(relationship.getType(), QName.class))
					.withId(relationship.getId())
					.withSourceElement(new TRelationshipTemplate.SourceElement().withRef(source))
					.withTargetElement(new TRelationshipTemplate.TargetElement().withRef(target)));
		}

		return relTemplates;
	}

}
