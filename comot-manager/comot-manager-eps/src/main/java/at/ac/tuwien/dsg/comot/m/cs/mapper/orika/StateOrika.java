package at.ac.tuwien.dsg.comot.m.cs.mapper.orika;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.SalsaEntity;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.comot.m.cs.mapper.IdResolver;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.OsuType;
import at.ac.tuwien.dsg.comot.model.type.State;

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
								UnitInstance existingInst;
								boolean add;

								for (ServiceInstance instance : unit.getInstancesList()) {
									existingInst = null;
									for (UnitInstance cInst : node.getInstances()) {
										if (instance.getInstanceId() == cInst.getInstanceId()) {
											existingInst = cInst;
										}
									}

									if (existingInst == null) {

										if (node.getOsu().getType().equals(OsuType.OS)) {
											// nInst = facade.map(instance, UnitInstanceOs.class);
											nInst = facade.map(instance, UnitInstance.class); // TODO remove and make it
																								// accept also IP
										} else {
											nInst = facade.map(instance, UnitInstance.class);
										}
										nInst.setId(IdResolver.uniqueInstance(unit.getId(), instance.getInstanceId()));

										node.addUnitInstance(nInst);
									} else {

										facade.map(instance, existingInst);
									}
								}
							}
						})
				.register();

		mapperFactory
				.classMap(UnitInstance.class, ServiceInstance.class)
				.field("instanceId", "instanceId")
				.field("state", "state")
				.register();
		// TODO get IP from SalsaInstanceDescription_VM

		// mapperFactory.classMap(UnitInstanceOs.class, ServiceInstance.class)
		// .field("instanceId", "instanceId")
		// .field("state", "state")
		// .customize(
		// new CustomMapper<UnitInstanceOs, ServiceInstance>() {
		// @Override
		// public void mapBtoA(ServiceInstance inst, UnitInstanceOs nodeInst, MappingContext context) {
		// if (inst.getProperties() != null && inst.getProperties().getAny() != null) {
		// facade.map(((SalsaInstanceDescription_VM) inst.getProperties().getAny()), nodeInst);
		// }
		// }
		// })
		// .register();
		//
		// mapperFactory.classMap(UnitInstanceOs.class, SalsaInstanceDescription_VM.class)
		// .field("provider", "provider")
		// .field("baseImage", "baseImage")
		// .field("instanceType", "instanceType")
		// .field("uuid", "instanceId")
		// .field("ip", "privateIp")
		// .register();

		ConverterFactory converterFactory = mapperFactory.getConverterFactory();
		converterFactory.registerConverter(new BidirectionalConverter<State, SalsaEntityState>() {

			@Override
			public SalsaEntityState convertTo(State source, Type<SalsaEntityState> destinationType) {
				switch (source) {
				case IDLE:
					return SalsaEntityState.UNDEPLOYED;
				case DEPLOYMENT_ALLOCATING:
					return SalsaEntityState.ALLOCATING;
				case DEPLOYMENT_CONFIGURING:
					return SalsaEntityState.CONFIGURING;
				case DEPLOYMENT_INSTALLING:
					return SalsaEntityState.INSTALLING;
				case DEPLOYMENT_STAGING:
					return SalsaEntityState.STAGING;
				case OPERATION_RUNNING:
					return SalsaEntityState.DEPLOYED;
				case ERROR:
					return SalsaEntityState.ERROR;
				default:
					return null;
				}
			}

			@Override
			public State convertFrom(SalsaEntityState source, Type<State> destinationType) {
				return StateOrika.convert(source);
			}
		});

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

	public static State convert(SalsaEntityState state) {
		switch (state) {
		case ALLOCATING:
			return State.DEPLOYMENT_ALLOCATING;
		case CONFIGURING:
			return State.DEPLOYMENT_CONFIGURING;
		case DEPLOYED:
			return State.OPERATION_RUNNING;
		case ERROR:
			return State.ERROR;
		case INSTALLING:
			return State.DEPLOYMENT_INSTALLING;
		case STAGING:
			return State.DEPLOYMENT_STAGING;
		case STAGING_ACTION:
			return State.STAGING_ACTION;
		case UNDEPLOYED:
			return State.IDLE;
		default:
			return null;
		}
	}
}
