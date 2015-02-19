package at.ac.tuwien.dsg.comot.m.cs.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import ma.glasnost.orika.MapperFacade;

import org.oasis.tosca.Definitions;
import org.oasis.tosca.TArtifactReference;
import org.oasis.tosca.TArtifactTemplate;
import org.oasis.tosca.TArtifactTemplate.ArtifactReferences;
import org.oasis.tosca.TCapability;
import org.oasis.tosca.TEntityTemplate;
import org.oasis.tosca.TExtensibleElements;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TNodeTemplate.Capabilities;
import org.oasis.tosca.TNodeTemplate.Requirements;
import org.oasis.tosca.TRelationshipTemplate;
import org.oasis.tosca.TRequirement;
import org.oasis.tosca.TServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.ToscaConverters;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.ToscaOrika;
import at.ac.tuwien.dsg.comot.model.devel.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.devel.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.devel.relationship.LocalRel;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.model.type.RelationshipType;

@Component
public class ToscaMapper {

	protected final Logger log = LoggerFactory.getLogger(ToscaMapper.class);

	// public static final String SUFFIX_PROPERTY_SALSA = "_property_salsa";
	protected static final QName CAP_REQ_TYPE = ToscaConverters.toSalsaQName("variable");

	@Autowired
	protected ToscaOrika mapper;

	protected MapperFacade maper;

	@PostConstruct
	public void build() {
		maper = mapper.get();
	}

	public Definitions extractTosca(CloudService cloudService) throws JAXBException {

		Definitions definition = mapper.get().map(cloudService, Definitions.class);
		Navigator navigator = new Navigator(cloudService);

		log.trace("Mapping by orica: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

		TArtifactTemplate tArtifact;
		ArtifactReferences refs;

		// inject TArtifactTemplates
		for (ServiceUnit unit : navigator.getAllUnits()) {
			
			if(!unit.getOsu().getType().equals(NodePropertiesType.OS) 
					&& unit.getOsu().getResources() != null 
					&& !unit.getOsu().getResources().isEmpty()){
				refs = new ArtifactReferences();
				tArtifact = mapper.get().map(unit.getOsu(), TArtifactTemplate.class);
				tArtifact.setArtifactReferences(refs);
	
				for (String uri : unit.getOsu().getResources().values()) {
					refs.withArtifactReference(new TArtifactReference().withReference(uri));
				}
				definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(tArtifact);
			}
		}

		String sourceTopoId;
		String targetTopoId;

		for (TExtensibleElements element : definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TServiceTemplate) {
				TServiceTemplate topology = (TServiceTemplate) element;

				// inject Relationships
				for (TRelationshipTemplate oneRel : createTRelationships(navigator.getAllUnits())) {

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

				for (TExtensibleElements element2 : topology.getTopologyTemplate()
						.getNodeTemplateOrRelationshipTemplate()) {
					if (element2 instanceof TNodeTemplate) {
						TNodeTemplate tNode = (TNodeTemplate) element2;

						// set capabilities
						Capabilities capas = new Capabilities();
						tNode.setCapabilities(capas);

						for (ConnectToRel rel : navigator.getUnit(tNode.getId()).getConnectTo()) {
							TCapability capa = new TCapability()
									.withId(rel.getCapabilityId())
									.withType(CAP_REQ_TYPE);
							capas.withCapability(capa);
						}

						// set requirements
						Requirements reqs = new Requirements();
						tNode.setRequirements(reqs);

						for (ServiceUnit one : navigator.getAllUnits()) {
							for (ConnectToRel rel : one.getConnectTo()) {
								if (rel.getTo().getId().equals(tNode.getId())) {
									TRequirement req = new TRequirement()
											.withId(rel.getRequirementId())
											.withType(CAP_REQ_TYPE);
									reqs.withRequirement(req);
								}
							}
						}
					}
				}

			}
		}

		log.debug("Final mapping: {}", Utils.asXmlString(definition, SalsaMappingProperties.class));

		return definition;
	}

	public CloudService createModel(Definitions definition) {

		String from, to;
		ServiceUnit startNode, endNode;
		Map<String, ServiceUnit> capaReq = new HashMap<>();
		CloudService service = maper.map(definition, CloudService.class);

		// log.trace("Mapping by orika: {}", Utils.asJsonString(cloudService));

		// 1. run
		for (TExtensibleElements element : definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			// TOPOLOGY
			if (element instanceof TServiceTemplate) {
				TServiceTemplate tTopology = (TServiceTemplate) element;

				ServiceTopology topology = maper.map(tTopology, ServiceTopology.class);
				service.addServiceTopology(topology);

				for (TExtensibleElements element2 : tTopology.getTopologyTemplate()
						.getNodeTemplateOrRelationshipTemplate()) {

					// NODE
					if (element2 instanceof TNodeTemplate) {

						TNodeTemplate tNode = (TNodeTemplate) element2;

						// create StackNode
						ServiceUnit node = maper.map(tNode, ServiceUnit.class);
						topology.addServiceUnit(node);

						// mark capabilities and requirements
						if (tNode.getCapabilities() != null) {
							for (TCapability capa : tNode.getCapabilities().getCapability()) {
								capaReq.put(capa.getId(), node);
							}
						}
						if (tNode.getRequirements() != null) {
							for (TRequirement req : tNode.getRequirements().getRequirement()) {
								capaReq.put(req.getId(), node);
							}
						}
					}
				}
			}
		}

		Navigator navigator = new Navigator(service);

		// 2. run
		for (TExtensibleElements element : definition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TServiceTemplate) {
				for (TExtensibleElements element2 : ((TServiceTemplate) element).getTopologyTemplate()
						.getNodeTemplateOrRelationshipTemplate()) {

					// RELATIONSHIP
					if (element2 instanceof TRelationshipTemplate) {

						TRelationshipTemplate rel = (TRelationshipTemplate) element2;

						from = ((TEntityTemplate) rel.getSourceElement().getRef()).getId();
						to = ((TEntityTemplate) rel.getTargetElement().getRef()).getId();
						RelationshipType type = RelationshipType.fromString(rel.getType().getLocalPart());

						if (type.equals(RelationshipType.CONNECT_TO)) {
							startNode = capaReq.get(from);
							endNode = capaReq.get(to);

							startNode.addConnectTo(new ConnectToRel(rel.getId(), from, to, startNode, endNode));

						} else {
							startNode = navigator.getUnitFor(from);
							endNode = navigator.getUnitFor(to);

							if (type.equals(RelationshipType.LOCAL)) {
								startNode.addLocal(new LocalRel(rel.getId(), startNode, endNode));

							} else if (type.equals(RelationshipType.HOST_ON)) {
								startNode.setHost(new HostOnRel(rel.getId(), startNode, endNode));
							}
						}
					}
				}

			} else if (element instanceof TArtifactTemplate) {
				TArtifactTemplate tArtifact = (TArtifactTemplate) element;

				OfferedServiceUnit artifact = maper.map(tArtifact, OfferedServiceUnit.class);
//			 TODO
//				for (TArtifactReference ref : tArtifact.getArtifactReferences().getArtifactReference()) {
//					artifact.addResource(tArtifact.getType().getLocalPart(), value);addUri(ref.getReference());
//				}
//
//				for (ServiceUnit node : navigator.getAllUnits()) {
//					if (node.getDeploymentArtifacts().contains(artifact)) {
//						node.getDeploymentArtifacts().remove(artifact);
//						node.addDeploymentArtifact(artifact);
//					}
//				}
			}
		}

		// log.debug("Final mapping: {}", Utils.asJsonString(cloudService));

		return service;
	}

	protected List<TRelationshipTemplate> createTRelationships(List<ServiceUnit> units) {

		List<TRelationshipTemplate> relTemplates = new ArrayList<>();

		for (ServiceUnit unit : units) {
			// CONNECT_TO
			for (ConnectToRel rel : unit.getConnectTo()) {
				relTemplates.add(createTRelationshps(rel.getId(), rel.getCapabilityId(), rel.getRequirementId(),
						RelationshipType.CONNECT_TO));
			}
			// LOCAL
			for (LocalRel rel : unit.getLocal()) {
				relTemplates.add(createTRelationshps(rel.getId(), unit.getId(), rel.getTo().getId(),
						RelationshipType.LOCAL));
			}
			// HOST_ON
			if (unit.getHost() != null) {
				relTemplates.add(createTRelationshps(unit.getHost().getId(), unit.getId(), unit.getHost()
						.getTo().getId(), RelationshipType.HOST_ON));
			}
		}

		return relTemplates;
	}

	protected TRelationshipTemplate createTRelationshps(String id, String from, String to, RelationshipType type) {

		// create mock object for referencing
		TEntityTemplate source = new TNodeTemplate().withId(from);
		TEntityTemplate target = new TNodeTemplate().withId(to);

		return new TRelationshipTemplate()
				.withType(mapper.get().map(type, QName.class))
				.withId(id)
				.withSourceElement(new TRelationshipTemplate.SourceElement().withRef(source))
				.withTargetElement(new TRelationshipTemplate.TargetElement().withRef(target));
	}

}
