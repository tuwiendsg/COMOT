package at.ac.tuwien.dsg.comot.cs.mapper.orika;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.common.model.logic.RelationshipResolver;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.unit.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstanceOs;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;

@Component
public class DeploymentOrika {

	protected final Logger log = LoggerFactory.getLogger(DeploymentOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(ServiceUnit.class, DeploymentUnit.class)
				.field("elasticityCapabilities", "elasticityCapabilities")
				.field("id", "serviceUnitID")
				.register();

		mapperFactory.classMap(StackNode.class, DeploymentUnit.class)
				// .field("deploymentInfo.defaultImage", "defaultImage")
				// .field("deploymentInfo.defaultFlavor", "defaultFlavor")
				.field("instances", "associatedVM")
				.register();

		mapperFactory.classMap(NodeInstanceOs.class, AssociatedVM.class)
				.field("ip", "ip")
				.field("uuid", "uuid")
				.register();

		mapperFactory
				.classMap(
						ElasticityCapability.class,
						at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.rSYBL.deploymentDescription.ElasticityCapability.class)
				.field("name", "name")
				.field("script", "script")
				.register();

		mapperFactory.classMap(CloudService.class, DeploymentDescription.class)
				.field("accessIp", "accessIP")
				.fieldAToB("id", "cloudServiceID")
				.customize(
						new CustomMapper<CloudService, DeploymentDescription>() {

							@Override
							public void mapAtoB(CloudService service, DeploymentDescription description,
									MappingContext context) {

								Navigator navigator = new Navigator(service);
								RelationshipResolver resolver = new RelationshipResolver(service);

								DeploymentUnit depl;

								for (ServiceUnit unit : navigator.getAllServiceUnits()) {

									StackNode os = resolver.getOsForServiceUnit(unit.getId());

									depl = facade.map(unit, DeploymentUnit.class);
									facade.map(os, depl);

									description.getDeployments().add(depl);
								}
							}
						})
				.byDefault()
				.register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

}
