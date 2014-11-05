package at.ac.tuwien.dsg.comot.cs.transformer;

import static org.dozer.loader.api.FieldsMappingOptions.hintA;
import static org.dozer.loader.api.FieldsMappingOptions.hintB;
import static org.dozer.loader.api.TypeMappingOptions.mapNull;

import javax.annotation.PostConstruct;

import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.dozer.loader.api.FieldDefinition;
import org.oasis.tosca.Definitions;
import org.oasis.tosca.TArtifactReference;
import org.oasis.tosca.TArtifactTemplate;
import org.oasis.tosca.TCapability;
import org.oasis.tosca.TDeploymentArtifact;
import org.oasis.tosca.TNodeTemplate;
import org.oasis.tosca.TPolicy;
import org.oasis.tosca.TRequirement;
import org.oasis.tosca.TServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.model.ArtifactReference;
import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.elastic.Capability;
import at.ac.tuwien.dsg.comot.common.model.elastic.Requirement;
import at.ac.tuwien.dsg.comot.common.model.elastic.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;

@Component
public class ToscaMapperDozer {

	protected final Logger log = LoggerFactory.getLogger(ToscaMapperDozer.class);

	// @Autowired
	// @Qualifier("dozerMapperXml")
	protected DozerBeanMapper xmlMapper;

	public DozerBeanMapper get() {
		return xmlMapper;
	}

	@PostConstruct
	public void build() {

		BeanMappingBuilder builder = new BeanMappingBuilder() {
			protected void configure() {
				mapping(SyblDirective.class, TPolicy.class, mapNull(false))
						.fields("directive", "name")
						.fields("type", "policyType");

				mapping(Capability.class, TCapability.class, mapNull(false));

				mapping(Requirement.class, TRequirement.class, mapNull(false));

				mapping(ArtifactTemplate.class, TDeploymentArtifact.class, mapNull(false))
						.fields("type", "artifactType") // TODO dp it qualified name
						.fields("id", "artifactRef");

				mapping(ServiceUnit.class, TNodeTemplate.class, mapNull(false))
						.fields("maxInstances", "maxInstances")
						.fields("minInstances",
								new FieldDefinition("minInstances").setMethod("setMinInstances(java.lang.Integer)"))
						.fields("capabilities", "capabilities.capability")
						.fields("requirements", "requirements.requirement")
						.fields("directives", "policies.policy")
						.fields("deploymentArtifacts", "deploymentArtifacts.deploymentArtifact",
								hintA(ArtifactTemplate.class),
								hintB(TDeploymentArtifact.class));

				mapping(ServiceTopology.class, TServiceTemplate.class, mapNull(false))
						.fields("serviceUnits", "topologyTemplate.nodeTemplateOrRelationshipTemplate");

				mapping(CloudService.class, Definitions.class, mapNull(false))
						.fields("serviceTopologies", "serviceTemplateOrNodeTypeOrNodeTypeImplementation",
								hintA(ServiceTopology.class),
								hintB(TServiceTemplate.class));

				// artifact creation
				mapping(ArtifactTemplate.class, TArtifactTemplate.class, mapNull(false))
						.fields("artifactReferences", "artifactReferences.artifactReference",
								hintA(ArtifactReference.class),
								hintB(TArtifactReference.class))
						.exclude("name");

				mapping(ArtifactReference.class, TArtifactReference.class, mapNull(false))
						.fields("uri", "reference");

			}
		};

		xmlMapper = new DozerBeanMapper();

		xmlMapper.addMapping(builder);
	}
	/*
	 * public class PropertiesConverter extends DozerConverter<HashMap, Properties> {
	 * 
	 * public PropertiesConverter() { super(HashMap.class, Properties.class); }
	 * 
	 * @Override public Properties convertTo(HashMap source, Properties destination) {
	 * 
	 * log.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
	 * 
	 * if(source == null){ return null; }
	 * 
	 * 
	 * 
	 * Map<String, String> osProperties = new HashMap<>(); osProperties.put("instanceType", source.getInstanceType());
	 * osProperties.put("provider", source.getProvider()); osProperties.put("baseImage", source.getBaseImage());
	 * osProperties.put("packages", Joiner.on(",").join(source.getPackages()));
	 * 
	 * SalsaMappingProperties salsaMappingProperties = new SalsaMappingProperties(); salsaMappingProperties.put("os",
	 * source);
	 * 
	 * Object properties = new Object(); properties.setAny(salsaMappingProperties);
	 * 
	 * 
	 * 
	 * return null; }
	 * 
	 * @Override public HashMap convertFrom(Properties source, HashMap destination) {
	 * log.info("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
	 * 
	 * if(source == null || source.getAny() == null){ return null; }
	 * 
	 * return (Map) source.getAny();
	 * 
	 * return null;
	 * 
	 * }
	 * 
	 * }
	 */
}
