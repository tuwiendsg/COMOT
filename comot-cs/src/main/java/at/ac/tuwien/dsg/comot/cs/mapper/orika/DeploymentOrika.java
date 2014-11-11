package at.ac.tuwien.dsg.comot.cs.mapper.orika;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.model.Navigator;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.unit.AssociatedVM;
import at.ac.tuwien.dsg.comot.common.model.unit.ElasticityCapability;
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
				.field("deploymentInfo.defaultImage", "defaultImage")
				.field("deploymentInfo.defaultFlavor", "defaultFlavor")
				.field("deploymentInfo.elasticityCapabilities", "elasticityCapabilities")
				.field("deploymentInfo.associatedVMs", "associatedVM")
				.fieldAToB("id", "serviceUnitID")
				.register();

		mapperFactory
				.classMap(
						AssociatedVM.class,
						at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.rSYBL.deploymentDescription.AssociatedVM.class)
				.byDefault()
				.register();

		mapperFactory
				.classMap(
						ElasticityCapability.class,
						at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.rSYBL.deploymentDescription.ElasticityCapability.class)
				.byDefault()
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
								List<DeploymentUnit> deployments = new ArrayList<>();
								DeploymentUnit depl;

								for (ServiceUnit unit : navigator.getAllServiceUnits()) {
									if (unit.getDeploymentInfo() != null) {
										depl = facade.map(unit, DeploymentUnit.class);
										deployments.add(depl);
									}
								}
								description.setDeployments(deployments);
							}

							@Override
							public void mapBtoA(DeploymentDescription description, CloudService service,
									MappingContext context) {

								Navigator navigator = new Navigator(service);
								List<DeploymentUnit> deployments = new ArrayList<>();
								ServiceUnit unit;

								for (DeploymentUnit depl : description.getDeployments()) {
									unit = navigator.getServiceUnit(depl.getServiceUnitID());
									if (unit != null) {
										facade.map(depl, unit);
									} else {
										log.error("There is no ServiceUnit for DeploymentUnit id={}",
												depl.getServiceUnitID());
									}
								}
								description.setDeployments(deployments);
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
