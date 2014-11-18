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

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.SalsaEntity;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstance;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstanceOs;

@Component
public class StateOrika {

	protected final Logger log = LoggerFactory.getLogger(StateOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		mapperFactory.classMap(ServicePart.class, SalsaEntity.class)
				.field("state", "state")
				.register();

		mapperFactory.classMap(CloudService.class,
				at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService.class)
				.register();

		mapperFactory.classMap(ServiceTopology.class,
				at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology.class)
				.register();

		mapperFactory
				.classMap(StackNode.class, at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit.class)
				.field("state", "state")
				.customize(
						new CustomMapper<StackNode, at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit>() {

							@Override
							public void mapBtoA(
									at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit unit,
									StackNode node, MappingContext context) {

								NodeInstance nInst;

								for (ServiceInstance instance : unit.getInstancesList()) {
									if (node.getType().equals(NodeType.OS)) {
										nInst = facade.map(instance, NodeInstanceOs.class);
									} else {
										nInst = facade.map(instance, NodeInstance.class);
									}
									node.addNodeInstance(nInst);
								}
							}
						})
				.register();

		mapperFactory
				.classMap(NodeInstance.class, ServiceInstance.class)
				.field("instanceId", "instanceId")
				.field("hostedId", "hostedId_Integer")
				.field("state", "state")
				.register();

		mapperFactory.classMap(NodeInstanceOs.class, ServiceInstance.class)
				.field("instanceId", "instanceId")
				.field("hostedId", "hostedId_Integer")
				.field("state", "state")
				.customize(
						new CustomMapper<NodeInstanceOs, ServiceInstance>() {
							@Override
							public void mapBtoA(ServiceInstance inst, NodeInstanceOs nodeInst, MappingContext context) {
								facade.map(((SalsaInstanceDescription_VM) inst.getProperties().getAny()), nodeInst);
							}
						})
				.register();

		mapperFactory.classMap(NodeInstanceOs.class, SalsaInstanceDescription_VM.class)
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
