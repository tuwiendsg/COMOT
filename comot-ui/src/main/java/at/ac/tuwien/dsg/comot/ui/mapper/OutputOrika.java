package at.ac.tuwien.dsg.comot.ui.mapper;

import java.util.ArrayList;
import java.util.List;

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

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstance;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstanceOs;
import at.ac.tuwien.dsg.comot.ui.model.Element;
import at.ac.tuwien.dsg.comot.ui.model.Element.State;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement.MonitoredElementLevel;

@Component
public class OutputOrika {

	protected final Logger log = LoggerFactory.getLogger(OutputOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		ConverterFactory converterFactory = mapperFactory.getConverterFactory();

		mapperFactory.classMap(ServicePart.class, Element.class)
				.field("id", "id")
				.field("state", "state")
				.register();

		mapperFactory.classMap(CloudService.class, Element.class)
				.field("serviceTopologies", "children")
				.customize(
						new CustomMapper<CloudService, Element>() {
							@Override
							public void mapAtoB(CloudService service, Element element, MappingContext context) {
								element.setType(Element.Type.SERVICE);
							}
						})
				.register();

		mapperFactory.classMap(ServiceTopology.class, Element.class)
				.customize(
						new CustomMapper<ServiceTopology, Element>() {
							@Override
							public void mapAtoB(ServiceTopology topology, Element element, MappingContext context) {
								element.setType(Element.Type.TOPOLOGY);

								for (ServiceTopology child : topology.getServiceTopologies()) {
									element.addChild(facade.map(child, Element.class));
								}

								for (StackNode node : topology.getNodes()) {
									if (node.getInstances().size() == 0) {
										Element one = facade.map(node, Element.class);
										one.setState(State.valueOf(node.getState().toString()));
										element.addChild(one);
									} else {
										for (NodeInstance instance : node.getInstances()) {
											Element one = facade.map(node, Element.class);
											facade.map(instance, one);
											element.addChild(one);
										}
									}
								}
							}
						})
				.register();

		mapperFactory.classMap(StackNode.class, Element.class)
				.field("id", "id")
				.fieldAToB("type", "type")
				.register();

		mapperFactory.classMap(NodeInstance.class, Element.class)
				.field("instanceId", "instanceId")
				.field("state", "state")
				.register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

}
