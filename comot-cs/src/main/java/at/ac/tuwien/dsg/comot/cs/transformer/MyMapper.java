package at.ac.tuwien.dsg.comot.cs.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.oasis.tosca.Definitions;
import org.oasis.tosca.TArtifactTemplate;
import org.oasis.tosca.TExtensibleElements;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TRelationshipTemplate;
import org.oasis.tosca.TServiceTemplate;
import org.oasis.tosca.TTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;

@Component
public class MyMapper {

	protected final Logger log = LoggerFactory.getLogger(MyMapper.class);

	@Autowired
	protected ToscaMapperDozer mapper;

	public Definitions toTosca(CloudService cloudService) throws JAXBException {

		Definitions definition = mapper.get().map(cloudService, Definitions.class);
		Navigator navigator = new Navigator(cloudService);

		log.debug("Mapping by dozer: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

		TArtifactTemplate tArtifact;

		// inject ArtifactTemplates
		for (ServiceUnit unit : navigator.getAllUnits(cloudService)) {
			for (ArtifactTemplate artifact : unit.getDeploymentArtifacts()) {

				tArtifact = mapper.get().map(artifact, TArtifactTemplate.class);
				definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(tArtifact);
			}
		}

		// inject Relationships
		List<TRelationshipTemplate> relTemplates = createTRelationshps(
				cloudService.getRelationships(), extractNodes(definition));

		String sourceTopoId;
		String targetTopoId;

		for (TExtensibleElements element : definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TServiceTemplate) {
				TServiceTemplate topology = (TServiceTemplate) element;

				for (TRelationshipTemplate oneRel : relTemplates) {

					sourceTopoId = navigator.getParentId(((TNodeTemplate) oneRel.getSourceElement().getRef()).getId());
					targetTopoId = navigator.getParentId(((TNodeTemplate) oneRel.getTargetElement().getRef()).getId());

					log.info("Inserted relationship id={}, from={}, to={}", oneRel.getId(), sourceTopoId, targetTopoId);

					if (sourceTopoId.equals(topology.getId()) || targetTopoId.equals(topology.getId())) {
						topology.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate().add(oneRel);
					}
				}
			}
		}

		return definition;
	}

	protected List<TRelationshipTemplate> createTRelationshps(
			List<EntityRelationship> relationships,
			Map<String, TNodeTemplate> referableElements) {

		List<TRelationshipTemplate> relTemplates = new ArrayList<>();

		for (EntityRelationship relationship : relationships) {

			TNodeTemplate source = referableElements.get(relationship.getFrom().getId());
			TNodeTemplate target = referableElements.get(relationship.getTo().getId());

			if (source != null && target != null) {

				relTemplates.add(new TRelationshipTemplate()
						.withType(new QName(relationship.getType()))
						.withId(relationship.getId())
						.withSourceElement(new TRelationshipTemplate.SourceElement().withRef(source))
						.withTargetElement(new TRelationshipTemplate.TargetElement().withRef(target)));

			} else {
				log.error("Either source or target of EntityRelationship {} is not available: source: {} target: {}",
						relationship, source, target);
				throw new IllegalStateException("Cannot resolve source or target element for relationship "
						+ relationship.getId());
			}
		}

		return relTemplates;

	}

	protected Map<String, TNodeTemplate> extractNodes(Definitions definition) {

		Map<String, TNodeTemplate> referableElements = new HashMap<>();

		for (TExtensibleElements element : definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TServiceTemplate) {

				TTopologyTemplate topology = ((TServiceTemplate) element).getTopologyTemplate();

				for (TExtensibleElements element2 : topology.getNodeTemplateOrRelationshipTemplate()) {
					if (element2 instanceof TNodeTemplate) {
						TNodeTemplate node = (TNodeTemplate) element2;

						referableElements.put(node.getId(), node);
					}
				}
			}
		}

		return referableElements;
	}

	// TODO fix types to be qualified
	// private QName buildArtifactTypeQname(ArtifactTemplate artifact) {
	// return new QName(DEFAULT_ARTIFACT_TYPE_NAMESPACE_URI, artifact.getType(), DEFAULT_ARTIFACT_TYPE_PREFIX);
	// }

}
