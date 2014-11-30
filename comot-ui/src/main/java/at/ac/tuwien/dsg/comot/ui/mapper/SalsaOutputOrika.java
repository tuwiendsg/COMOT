package at.ac.tuwien.dsg.comot.ui.mapper;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstance;
import at.ac.tuwien.dsg.comot.ui.model.ElementState;
import at.ac.tuwien.dsg.comot.ui.model.ElementState.State;

@Component
public class SalsaOutputOrika {

	protected final Logger log = LoggerFactory.getLogger(SalsaOutputOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		ConverterFactory converterFactory = mapperFactory.getConverterFactory();

		mapperFactory.classMap(ServicePart.class, ElementState.class)
				.field("id", "id")
				.field("state", "state")
				.register();

		mapperFactory.classMap(CloudService.class, ElementState.class)
				.field("serviceTopologies", "children")
				.customize(
						new CustomMapper<CloudService, ElementState>() {
							@Override
							public void mapAtoB(CloudService service, ElementState element, MappingContext context) {
								element.setType(ElementState.Type.SERVICE);
							}
						})
				.register();

		mapperFactory.classMap(ServiceTopology.class, ElementState.class)
				.customize(
						new CustomMapper<ServiceTopology, ElementState>() {
							@Override
							public void mapAtoB(ServiceTopology topology, ElementState element, MappingContext context) {
								element.setType(ElementState.Type.TOPOLOGY);

								for (ServiceTopology child : topology.getServiceTopologies()) {
									element.addChild(facade.map(child, ElementState.class));
								}

								for (StackNode node : topology.getNodes()) {
									if (node.getInstances().size() == 0) {
										ElementState one = facade.map(node, ElementState.class);
										one.setState(State.valueOf(node.getState().toString()));
										element.addChild(one);
									} else {
										for (NodeInstance instance : node.getInstances()) {
											ElementState one = facade.map(node, ElementState.class);
											facade.map(instance, one);
											element.addChild(one);
										}
									}
								}
							}
						})
				.register();

		mapperFactory.classMap(StackNode.class, ElementState.class)
				.field("id", "id")
				.fieldAToB("type", "type")
				.register();

		mapperFactory.classMap(NodeInstance.class, ElementState.class)
				.field("instanceId", "instanceId")
				.field("state", "state")
				.register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

}
