package at.ac.tuwien.dsg.comot.cs.mapper.orika;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.oasis.tosca.Definitions;
import org.oasis.tosca.TArtifactReference;
import org.oasis.tosca.TArtifactTemplate;
import org.oasis.tosca.TCapability;
import org.oasis.tosca.TDeploymentArtifact;
import org.oasis.tosca.TEntityTemplate;
import org.oasis.tosca.TExtensibleElements;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TPolicy;
import org.oasis.tosca.TRequirement;
import org.oasis.tosca.TServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.type.ServiceUnitPropertiesType;
import at.ac.tuwien.dsg.comot.common.model.unit.ArtifactReference;
import at.ac.tuwien.dsg.comot.common.model.unit.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.unit.Capability;
import at.ac.tuwien.dsg.comot.common.model.unit.Properties;
import at.ac.tuwien.dsg.comot.common.model.unit.Requirement;

@Component
public class ToscaOrika {

	protected final Logger log = LoggerFactory.getLogger(ToscaOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(CloudService.class, Definitions.class)
				.byDefault()
				.register();

		mapperFactory.classMap(SyblDirective.class, TPolicy.class)
				.field("directive", "name")
				.field("type", "policyType")
				.byDefault()
				.register();

		mapperFactory.classMap(Capability.class, TCapability.class)
				.byDefault()
				.register();

		mapperFactory.classMap(Requirement.class, TRequirement.class);

		mapperFactory.classMap(ArtifactTemplate.class, TDeploymentArtifact.class)
				.field("type", "artifactType")
				.field("id", "artifactRef")
				.byDefault()
				.register();

		mapperFactory.classMap(ServiceUnit.class, TNodeTemplate.class)
				.field("maxInstances", "maxInstances")
				.field("capabilities", "capabilities.capability")
				.field("requirements", "requirements.requirement")
				.field("directives", "policies.policy")
				.field("deploymentArtifacts", "deploymentArtifacts.deploymentArtifact")
				.byDefault()
				.customize(
						new CustomMapper<ServiceUnit, TNodeTemplate>() {
							@Override
							public void mapAtoB(ServiceUnit unit, TNodeTemplate node, MappingContext context) {
								// do this manually because of mismatch of JAXB generated getter/setter int/Integer
								node.setMinInstances(unit.getMinInstances());

								// inserting SalsaMappingProperties into Object
								Properties props = unit.getProperties();
								if (props != null) {
									SalsaMappingProperties salsaProps = new SalsaMappingProperties();
									// TODO tell Hung about inconsistent types. Here it is just String, but in
									// documentation as QName
									salsaProps.put(props.getPropertiesType().toString(),
											props.getProperties());

									node.setProperties(new TEntityTemplate.Properties().withAny(salsaProps));
								}
							}

							@Override
							public void mapBtoA(TNodeTemplate node, ServiceUnit unit, MappingContext context) {

								if (node.getProperties() != null
										&& node.getProperties().getAny() != null
										&& node.getProperties().getAny() instanceof SalsaMappingProperties) {

									SalsaMappingProperties salsaProps = (SalsaMappingProperties) node.getProperties()
											.getAny();

									// TODO: change model properties to list so that it fits all from tosca
									unit.setProperties(new Properties(
											ServiceUnitPropertiesType.fromString(salsaProps.getProperties().get(0)
													.getType()),
											salsaProps.getProperties().get(0).getMapData())
											);
								}
							}
						})
				.register();

		mapperFactory.classMap(ServiceTopology.class, TServiceTemplate.class)
				.fieldAToB("serviceUnits", "topologyTemplate.nodeTemplateOrRelationshipTemplate")
				.customize(// custom mapper because of inheritance of TNodeTemplate
						new CustomMapper<ServiceTopology, TServiceTemplate>() {
							@Override
							public void mapBtoA(TServiceTemplate tServiceTemplate, ServiceTopology topology,
									MappingContext context) {

								for (TExtensibleElements element : tServiceTemplate.getTopologyTemplate()
										.getNodeTemplateOrRelationshipTemplate()) {
									if (element instanceof TNodeTemplate) {
										topology.addUnit(facade.map(element, ServiceUnit.class));
									}
								}
							}
						})
				.field("directives", "boundaryDefinitions.policies.policy")
				.byDefault()
				.register();

		mapperFactory.classMap(CloudService.class, Definitions.class)
				.fieldAToB("serviceTopologies", "serviceTemplateOrNodeTypeOrNodeTypeImplementation")
				.customize(// custom mapper because of inheritance of TServiceTemplate
						new CustomMapper<CloudService, Definitions>() {
							@Override
							public void mapBtoA(Definitions definition, CloudService service, MappingContext context) {

								for (TExtensibleElements element : definition
										.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
									if (element instanceof TServiceTemplate) {
										service.addServiceTopology(facade.map(element, ServiceTopology.class));
									}
								}
							}
						})

				.byDefault()
				.register();

		// artifact creation
		mapperFactory.classMap(ArtifactTemplate.class, TArtifactTemplate.class)
				.field("artifactReferences", "artifactReferences.artifactReference")
				.exclude("name")
				.byDefault()
				.register();

		mapperFactory.classMap(ArtifactReference.class, TArtifactReference.class)
				.field("uri", "reference")
				.byDefault()
				.register();

		ConverterFactory converterFactory = mapperFactory.getConverterFactory();
		converterFactory.registerConverter(new ToscaConverters.CapabilityTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.ServiceUnitTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.DirectiveTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.ArtifactTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.RequirementTypeConverter());
		converterFactory.registerConverter(new ToscaConverters.RelationshipTypeConverter());

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

}
