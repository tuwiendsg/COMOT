package at.ac.tuwien.dsg.comot.cs.mapper.orika;

import java.util.List;
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
import org.oasis.tosca.TEntityTemplate;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TPolicy;
import org.oasis.tosca.TServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties.SalsaMappingProperty;
import at.ac.tuwien.dsg.comot.cs.mapper.IdResolver;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.model.node.Properties;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;

@Component
public class ToscaOrika {

	protected final Logger log = LoggerFactory.getLogger(ToscaOrika.class);
	// public static final String OS = "os";
	public static final QName ATTRIBUTE_ID = new QName("id");

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(ServiceUnit.class, TNodeTemplate.Policies.class)
				.field("directives", "policy")
				.register();

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

		mapperFactory.classMap(ArtifactTemplate.class, TDeploymentArtifact.class)
				.field("type", "artifactType")
				.field("id", "artifactRef")
				.byDefault()
				.register();

		// artifact creation
		mapperFactory.classMap(ArtifactTemplate.class, TArtifactTemplate.class)
				.field("id", "id")
				.field("type", "type")
				.register();

		mapperFactory.classMap(StackNode.class, TNodeTemplate.class)
				.field("id", "id")
				.field("name", "name")
				.field("type", "type")
				.field("deploymentArtifacts", "deploymentArtifacts.deploymentArtifact")
				.customize(new NodeMapper())
				.register();

		mapperFactory.classMap(ServiceTopology.class, TServiceTemplate.class)
				.field("id", "id")
				.field("name", "name")
				.field("directives", "boundaryDefinitions.policies.policy")
				.fieldAToB("nodes", "topologyTemplate.nodeTemplateOrRelationshipTemplate")
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

	class NodeMapper extends CustomMapper<StackNode, TNodeTemplate> {
		@Override
		public void mapAtoB(StackNode unit, TNodeTemplate node, MappingContext context) {

			if (unit.getMaxInstances() == Integer.MAX_VALUE) {
				node.setMaxInstances("unbounded");
			} else {
				node.setMaxInstances(new Integer(unit.getMaxInstances()).toString());
			}
			// do this manually because of mismatch of JAXB generated getter/setter int/Integer
			node.setMinInstances(unit.getMinInstances());

			// inserting SalsaMappingProperties into Object
			Set<Properties> props = unit.getProperties();
			SalsaMappingProperties salsaProps = new SalsaMappingProperties();
			if (props != null) {
				for (Properties oneProps : props) {
					salsaProps.put(oneProps.getPropertiesType().toString(), oneProps.getPropertiesMap());
				}
				node.setProperties(new TEntityTemplate.Properties().withAny(salsaProps));
			}

			// map type
			node.setType(ToscaConverters.toSalsaQName(((StackNode) unit).getType().toString()));
		}

		@Override
		public void mapBtoA(TNodeTemplate node, StackNode unit, MappingContext context) {

			// min / max
			if (("unbounded").equals(node.getMaxInstances())) {
				unit.setMaxInstances(Integer.MAX_VALUE);
			} else {
				unit.setMaxInstances(new Integer(node.getMaxInstances()));
			}
			unit.setMinInstances(node.getMinInstances());

			// properties
			if (node.getProperties() != null
					&& node.getProperties().getAny() != null
					&& node.getProperties().getAny() instanceof SalsaMappingProperties) {

				List<SalsaMappingProperty> list = ((SalsaMappingProperties) node.getProperties()
						.getAny()).getProperties();

				if (list != null) {
					for (SalsaMappingProperty property : list) {
						unit.addProperties(new Properties(IdResolver.nodeToProperty(node.getId()),
								NodePropertiesType.fromString(property.getType()),
								property.getMapData()));
					}
				}
			}
		}
	}

}
