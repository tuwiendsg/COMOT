package at.ac.tuwien.dsg.comot.m.cs.mapper.orika;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.OsuType;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;

@Component
public class DeploymentOrika {

	protected final Logger log = LoggerFactory.getLogger(DeploymentOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
				.build();

		mapperFactory.classMap(ServiceUnit.class, DeploymentUnit.class)
				// .field("elasticityCapabilities", "elasticityCapabilities")
				.field("id", "serviceUnitID")
				.field("instances", "associatedVMs")
				.customize(new CustomMapper<ServiceUnit, DeploymentUnit>() {

					@Override
					public void mapAtoB(ServiceUnit unit, DeploymentUnit depl,
							MappingContext context) {

						if (unit.getOsu().getType().equals(OsuType.OS)) {
							for (UnitInstance instance : unit.getInstances()) {
								facade.map(instance, AssociatedVM.class);
							}
						}
					}
				}).register();

		mapperFactory.classMap(UnitInstance.class, AssociatedVM.class)
				.field("envId", "ip")
				// .field("uuid", "uuid") TODO not sure what to do with this
				.register();

		mapperFactory
				.classMap(
						ElasticityCapability.class,
						at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.rSYBL.deploymentDescription.ElasticityCapability.class)
				.field("name", "name").field("script", "script").register();

		mapperFactory
				.classMap(CloudService.class, DeploymentDescription.class)
				.field("accessIp", "accessIP")
				.fieldAToB("id", "cloudServiceID")
				.customize(
						new CustomMapper<CloudService, DeploymentDescription>() {

							@Override
							public void mapAtoB(CloudService service,
									DeploymentDescription description,
									MappingContext context) {

								Navigator navigator = new Navigator(service);

								DeploymentUnit depl;

								for (ServiceUnit unit : navigator.getAllUnits()) {

									if (!navigator.isTrueServiceUnit(unit
											.getId())) {
										continue;
									}

									ServiceUnit os = navigator
											.getOsForServiceUnit(unit.getId());

									depl = facade.map(unit,
											DeploymentUnit.class);
									facade.map(os, depl);

									description.getDeployments().add(depl);
								}
							}
						}).byDefault().register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

}
