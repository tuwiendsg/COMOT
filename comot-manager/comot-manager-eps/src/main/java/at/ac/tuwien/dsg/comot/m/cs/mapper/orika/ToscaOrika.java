package at.ac.tuwien.dsg.comot.m.cs.mapper.orika;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.oasis.tosca.Definitions;
import org.oasis.tosca.TArtifactTemplate;
import org.oasis.tosca.TDeploymentArtifact;
import org.oasis.tosca.TDeploymentArtifacts;
import org.oasis.tosca.TEntityTemplate;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TPolicy;
import org.oasis.tosca.TServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties.SalsaMappingProperty;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties.SalsaMappingProperty.Property;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.model.type.ResourceType;

@Component
public class ToscaOrika {

	protected final Logger log = LoggerFactory.getLogger(ToscaOrika.class);
	// public static final String OS = "os";
	public static final QName ATTRIBUTE_ID = new QName("id");

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(SyblDirective.class, TPolicy.class)
				.field("directive", "name")
				.field("type", "policyType")
				.customize(
						new CustomMapper<SyblDirective, TPolicy>() {
							@Override
							public void mapAtoB(SyblDirective dir, TPolicy policy, MappingContext context) {
								policy.getOtherAttributes().put(ATTRIBUTE_ID, dir.getId());
							}

							@Override
							public void mapBtoA(TPolicy policy, SyblDirective dir, MappingContext context) {
								dir.setId(policy.getOtherAttributes().get(ATTRIBUTE_ID));
							}
						})
				.register();

		mapperFactory.classMap(Resource.class, TDeploymentArtifact.class)
				.field("type.name", "artifactType")
				.field("name", "artifactRef")
				// .field("name", "name")
				.register();

		// artifact creation
		mapperFactory.classMap(Resource.class, TArtifactTemplate.class)
				.field("name", "id")
				.field("type.name", "type")
				.register();

		mapperFactory.classMap(ServiceUnit.class, TNodeTemplate.class)
				.field("id", "id")
				.field("name", "name")
				.field("osu.type", "type")
				.field("directives", "policies.policy")
				.customize(new NodeMapper())
				.register();

		mapperFactory.classMap(ServiceTopology.class, TServiceTemplate.class)
				.field("id", "id")
				.field("name", "name")
				.field("directives", "boundaryDefinitions.policies.policy")
				.fieldAToB("serviceUnits", "topologyTemplate.nodeTemplateOrRelationshipTemplate")
				.register();

		mapperFactory.classMap(CloudService.class, Definitions.class)
				.field("id", "id")
				.field("name", "name")
				.fieldAToB("serviceTopologies", "serviceTemplateOrNodeTypeOrNodeTypeImplementation")
				.register();

		ConverterFactory converterFactory = mapperFactory.getConverterFactory();
		converterFactory.registerConverter(new ToscaConverters.NodeTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.DirectiveTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.ArtifactTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.RelationshipTypeConverter());

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

	class NodeMapper extends CustomMapper<ServiceUnit, TNodeTemplate> {
		@Override
		public void mapAtoB(ServiceUnit unit, TNodeTemplate node, MappingContext context) {

			if (unit.getMaxInstances() == Integer.MAX_VALUE) {
				node.setMaxInstances("unbounded");
			} else {
				node.setMaxInstances(new Integer(unit.getMaxInstances()).toString());
			}
			// do this manually because of mismatch of JAXB generated getter/setter int/Integer
			node.setMinInstances(unit.getMinInstances());

			Set<PrimitiveOperation> operations = unit.getOsu().getPrimitiveOperations();
			Set<Resource> resources = unit.getOsu().getResources();
			SalsaMappingProperties salsaProps = new SalsaMappingProperties();
			TDeploymentArtifacts arts = new TDeploymentArtifacts();
			Map<String, String> map = new HashMap<>();

			// primitiveOperation -> property type action
			if (operations != null && !operations.isEmpty()) {
				for (PrimitiveOperation oneProps : operations) {
					map.put(oneProps.getName(), oneProps.getExecuteMethod());
				}
				salsaProps.put(NodePropertiesType.ACTION.toString(), map);
			}

			if (resources != null && !resources.isEmpty()) {
				for (Resource resource : resources) {

					// resource -> deploymentArtifact
					if (isArtifact(resource)) {
						arts.withDeploymentArtifact(facade.map(resource, TDeploymentArtifact.class));

						// resource -> property type os
					} else {
						salsaProps.put(NodePropertiesType.OS.toString(), resource.getType().getName(),
								resource.getName());
					}
				}
			}

			node.setProperties(new TEntityTemplate.Properties().withAny(salsaProps));
			node.setDeploymentArtifacts(arts);
		}

		@Override
		public void mapBtoA(TNodeTemplate node, ServiceUnit unit, MappingContext context) {

			// min / max
			if (("unbounded").equals(node.getMaxInstances())) {
				unit.setMaxInstances(Integer.MAX_VALUE);
			} else {
				unit.setMaxInstances(new Integer(node.getMaxInstances()));
			}
			unit.setMinInstances(node.getMinInstances());

			// osu name
			unit.getOsu().setName(unit.getId());

			log.info("aaa {}", unit.getId());
			// properties
			if (node.getProperties() != null
					&& node.getProperties().getAny() != null) {
				// && node.getProperties().getAny() instanceof SalsaMappingProperties) {

				log.info("bbb {}", unit.getId());
				log.info("ccc {}", node.getProperties().getAny());

				List<SalsaMappingProperty> list = ((SalsaMappingProperties) node.getProperties()
						.getAny()).getProperties();

				if (list != null) {
					for (SalsaMappingProperty property : list) {

						// os -> resource
						if (property.getType().equals(NodePropertiesType.OS.toString())) {

							log.info("xxxxxxx {}", unit.getId());

							for (Property prop : property.getPropertiesList()) {
								unit.getOsu().hasResource(
										new Resource(prop.getValue(), new ResourceOrQualityType(prop.getName())));
							}

							// action -> primitiveOperation
						} else if (property.getType().equals(NodePropertiesType.OS.toString())) {

							for (Property prop : property.getPropertiesList()) {
								unit.getOsu().hasPrimitiveOperation(
										new PrimitiveOperation(prop.getName(), prop.getValue()));
							}

						}
					}
				}
			}

			if (node.getDeploymentArtifacts() != null && node.getDeploymentArtifacts().getDeploymentArtifact() != null) {

				for (TDeploymentArtifact art : node.getDeploymentArtifacts().getDeploymentArtifact()) {
					unit.getOsu().hasResource(
							new Resource(art.getArtifactRef().getLocalPart(), new ResourceOrQualityType(art
									.getArtifactType().getLocalPart())));
				}
			}

		}
	}

	public static boolean isArtifact(Resource resource) {
		ResourceType type = ResourceType.fromString(resource.getType().getName());

		if (type.equals(ResourceType.APT_GET_COMMAND)
				|| type.equals(ResourceType.CHEF)
				|| type.equals(ResourceType.CHEF_SOLO)
				|| type.equals(ResourceType.SCRIPT)
				|| type.equals(ResourceType.WAR_FILE)) {
			return true;
		} else {
			return false;
		}

	}

}
