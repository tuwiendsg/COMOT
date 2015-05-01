/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
import at.ac.tuwien.dsg.comot.m.cs.mapper.IdResolver;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.type.ResourceType;

@Component
public class ToscaOrika {

	private static final Logger LOG = LoggerFactory.getLogger(ToscaOrika.class);
	public static final QName ATTRIBUTE_ID = new QName("id");

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(SyblDirective.class, TPolicy.class)
				// .field("directive", "name")
				.field("type", "policyType")
				.customize(
						new CustomMapper<SyblDirective, TPolicy>() {
							@Override
							public void mapAtoB(SyblDirective dir, TPolicy policy, MappingContext context) {

								policy.setName(dir.getId() + RsyblOrika.SEPARATOR_ID + dir.getDirective());
								// policy.getOtherAttributes().put(ATTRIBUTE_ID, dir.getId());
							}

							@Override
							public void mapBtoA(TPolicy policy, SyblDirective dir, MappingContext context) {

								int separatorIndex = policy.getName().indexOf(RsyblOrika.SEPARATOR_ID);
								dir.setId(policy.getName().substring(0, separatorIndex));
								dir.setDirective(policy.getName().substring(separatorIndex + 1));
								// dir.setId(policy.getOtherAttributes().get(ATTRIBUTE_ID));
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
				.field("osuInstance.osu.type", "type")
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

			LOG.trace("{}", unit);

			if (unit.getMaxInstances() == Integer.MAX_VALUE) {
				node.setMaxInstances("unbounded");
			} else {
				node.setMaxInstances(new Integer(unit.getMaxInstances()).toString());
			}
			// do this manually because of mismatch of JAXB generated getter/setter int/Integer
			node.setMinInstances(unit.getMinInstances());

			Set<PrimitiveOperation> operations = unit.getOsuInstance().getOsu().getPrimitiveOperations();
			Set<Resource> resources = unit.getOsuInstance().getOsu().getResources();
			SalsaMappingProperties salsaProps = new SalsaMappingProperties();
			TDeploymentArtifacts arts = new TDeploymentArtifacts();
			Map<String, String> map = new HashMap<>();

			// primitiveOperation -> property type action
			if (operations != null && !operations.isEmpty()) {
				for (PrimitiveOperation oneProps : operations) {
					map.put(oneProps.getName(), oneProps.getExecuteMethod());
				}
				salsaProps.put(ResourceType.ACTION.toString(), map);
			}

			if (resources != null && !resources.isEmpty()) {
				for (Resource resource : resources) {

					// resource -> deploymentArtifact
					if (isArtifact(resource)) {
						arts.withDeploymentArtifact(facade.map(resource, TDeploymentArtifact.class));
						node.setDeploymentArtifacts(arts);
						// resource -> property type os
					} else {
						salsaProps.put(ResourceType.OS.toString(), resource.getType().getName(),
								resource.getName());
					}
				}
			}

			if (!salsaProps.getProperties().isEmpty()) {
				node.setProperties(new TEntityTemplate.Properties().withAny(salsaProps));
			}

		}

		@Override
		public void mapBtoA(TNodeTemplate node, ServiceUnit unit, MappingContext context) {

			// min / max
			if (("unbounded").equals(node.getMaxInstances())) {
				unit.setMaxInstances(Integer.MAX_VALUE);
			} else {
				unit.setMaxInstances(Integer.valueOf(node.getMaxInstances()));
			}
			unit.setMinInstances(node.getMinInstances());

			// osu name
			unit.getOsuInstance().getOsu().setName(unit.getId());
			unit.getOsuInstance().setId(IdResolver.osuInstanceFromUnit(unit.getId()));
			unit.getOsuInstance().getOsu().setId(IdResolver.osuFromUnit(unit.getId()));

			// properties
			if (node.getProperties() != null
					&& node.getProperties().getAny() != null) {
				// && node.getProperties().getAny() instanceof SalsaMappingProperties) {

				List<SalsaMappingProperty> list = ((SalsaMappingProperties) node.getProperties()
						.getAny()).getProperties();

				if (list != null) {
					for (SalsaMappingProperty property : list) {

						// os -> resource
						if (property.getType().equals(ResourceType.OS.toString())) {

							for (Property prop : property.getPropertiesList()) {
								unit.getOsuInstance().getOsu().hasResource(
										new Resource(prop.getValue(), new ResourceOrQualityType(prop.getName())));
							}

							// action -> primitiveOperation
						} else if (property.getType().equals(ResourceType.ACTION.toString())) {

							for (Property prop : property.getPropertiesList()) {
								unit.getOsuInstance().getOsu().hasPrimitiveOperation(
										new PrimitiveOperation(prop.getName(), prop.getValue()));
							}

						}
					}
				}
			}

			if (node.getDeploymentArtifacts() != null && node.getDeploymentArtifacts().getDeploymentArtifact() != null) {

				for (TDeploymentArtifact art : node.getDeploymentArtifacts().getDeploymentArtifact()) {
					unit.getOsuInstance().getOsu().hasResource(
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
				|| type.equals(ResourceType.WAR_FILE)
				|| type.equals(ResourceType.MISC)) {
			return true;
		} else {
			return false;
		}

	}

}
