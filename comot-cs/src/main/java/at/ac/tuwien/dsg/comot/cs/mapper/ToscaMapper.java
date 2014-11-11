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
import at.ac.tuwien.dsg.comot.common.model.Navigator;
import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
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

		log.trace("Mapping by dozer: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

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
							((TEntityTemplate) oneRel.getSourceElement().getRef())
									.getId()).getId();
					targetTopoId = navigator.getParentTopologyFor(
							((TEntityTemplate) oneRel.getTargetElement().getRef())
									.getId()).getId();

					log.trace("Inserted relationship id={}, from={}, to={}", oneRel.getId(), sourceTopoId, targetTopoId);

					if (sourceTopoId.equals(topology.getId()) || targetTopoId.equals(topology.getId())) {
						topology.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate().add(oneRel);
					}
				}

			}
		}

		log.trace("Final mapping: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

		return definition;
	}

	public CloudService createModel(Definitions definitions) {

		ArtifactTemplate artifact;
		ReferencableEntity from;
		ReferencableEntity to;

		CloudService cloudService = mapper.get().map(definitions, CloudService.class);
		log.trace("Mapping by dozer: {}", Utils.asJsonString(cloudService));

		Navigator navigator = new Navigator(cloudService);

		for (TExtensibleElements element : definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			// inject ArtifactTemplates
			if (element instanceof TArtifactTemplate) {

				artifact = mapper.get().map((TArtifactTemplate) element, ArtifactTemplate.class);

				for (StackNode unit : navigator.getAllNodes()) {
					for (ArtifactTemplate currentArt : unit.getDeploymentArtifacts()) {
						if (currentArt.getId().equals(artifact.getId())) {
							currentArt.setArtifactReferences(artifact.getArtifactReferences());
						}
					}
				}

			} else if (element instanceof TServiceTemplate) {

				// inject relationships
				for (TExtensibleElements element2 : ((TServiceTemplate) element).getTopologyTemplate()
						.getNodeTemplateOrRelationshipTemplate()) {
					if (element2 instanceof TRelationshipTemplate) {
						TRelationshipTemplate rel = (TRelationshipTemplate) element2;

						if (!cloudService.containsRelationship(rel.getId())) {
							from = navigator.getReferencableEntity(((TEntityTemplate) rel.getSourceElement().getRef())
									.getId());
							to = navigator.getReferencableEntity(((TEntityTemplate) rel.getTargetElement()
									.getRef())
									.getId());

							cloudService
									.addEntityRelationship(new EntityRelationship(
											rel.getId(), RelationshipType.fromString(rel.getType().getLocalPart()),
											from, to, navigator.resolveToServicePart(from), navigator
													.resolveToServicePart(to)));
							// navigator.resolveServicePart(from),
							// navigator.resolveServicePart(to)));
						}
					}
				}

			}
		}

		log.trace("Final mapping: {}", Utils.asJsonString(cloudService));

		return cloudService;
	}

	protected List<TRelationshipTemplate> createTRelationshps(List<EntityRelationship> relationships) {

		List<TRelationshipTemplate> relTemplates = new ArrayList<>();

		for (EntityRelationship relationship : relationships) {

			// create mock object for referencing
			TEntityTemplate source = new TNodeTemplate().withId(relationship.getFrom().getId());
			TEntityTemplate target = new TNodeTemplate().withId(relationship.getTo().getId());

			relTemplates.add(new TRelationshipTemplate()
					.withType(mapper.get().map(relationship.getType(), QName.class))
					.withId(relationship.getId())
					.withSourceElement(new TRelationshipTemplate.SourceElement().withRef(source))
					.withTargetElement(new TRelationshipTemplate.TargetElement().withRef(target)));
		}

		return relTemplates;
	}

}
