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

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.SalsaEntity;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
import at.ac.tuwien.dsg.comot.m.cs.mapper.IdResolver;
import at.ac.tuwien.dsg.comot.model.node.UnitInstance;
import at.ac.tuwien.dsg.comot.model.node.UnitInstanceOs;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.NodeType;

@Component
public class StateOrika {

	protected final Logger log = LoggerFactory.getLogger(StateOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(ServiceEntity.class, SalsaEntity.class)
				.field("state", "state")
				.register();

		mapperFactory.classMap(CloudService.class,
				at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService.class)
				.register();

		mapperFactory.classMap(ServiceTopology.class,
				at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology.class)
				.register();

		mapperFactory
				.classMap(ServiceUnit.class, at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit.class)
				.field("state", "state")
				.customize(
						new CustomMapper<ServiceUnit, at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit>() {

							@Override
							public void mapBtoA(
									at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit unit,
									ServiceUnit node, MappingContext context) {

								UnitInstance nInst;

								for (ServiceInstance instance : unit.getInstancesList()) {
									if (node.getType().equals(NodeType.OS)) {
										nInst = facade.map(instance, UnitInstanceOs.class);
									} else {
										nInst = facade.map(instance, UnitInstance.class);
									}
									nInst.setId(IdResolver.uniqueInstance(unit.getId(), instance.getInstanceId()));
									node.addNodeInstance(nInst);
								}
							}
						})
				.register();

		mapperFactory
				.classMap(UnitInstance.class, ServiceInstance.class)
				.field("instanceId", "instanceId")
				.field("state", "state")
				.register();

		mapperFactory.classMap(UnitInstanceOs.class, ServiceInstance.class)
				.field("instanceId", "instanceId")
				.field("state", "state")
				.customize(
						new CustomMapper<UnitInstanceOs, ServiceInstance>() {
							@Override
							public void mapBtoA(ServiceInstance inst, UnitInstanceOs nodeInst, MappingContext context) {
								if (inst.getProperties() != null && inst.getProperties().getAny() != null) {
									facade.map(((SalsaInstanceDescription_VM) inst.getProperties().getAny()), nodeInst);
								}
							}
						})
				.register();

		mapperFactory.classMap(UnitInstanceOs.class, SalsaInstanceDescription_VM.class)
				.field("provider", "provider")
				.field("baseImage", "baseImage")
				.field("instanceType", "instanceType")
				.field("uuid", "instanceId")
				.field("ip", "privateIp")
				.register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

}
