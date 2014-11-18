package at.ac.tuwien.dsg.comot.cs.mapper.orika;

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

import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement.MonitoredElementLevel;

@Component
public class MelaOrika {

	protected final Logger log = LoggerFactory.getLogger(MelaOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		ConverterFactory converterFactory = mapperFactory.getConverterFactory();

		mapperFactory.classMap(CloudService.class, MonitoredElement.class)
				.field("id", "id")
				.field("name", "name")
				.field("serviceTopologies", "containedElements")
				.exclude("relationships")
				.customize(
						new CustomMapper<CloudService, MonitoredElement>() {
							@Override
							public void mapAtoB(CloudService service, MonitoredElement element, MappingContext context) {
								// set level
								element.setLevel(decideLevel(service));
							}
						})
				.register();

		mapperFactory.classMap(ServiceTopology.class, MonitoredElement.class)
				.field("id", "id")
				.field("name", "name")
				.customize(
						new CustomMapper<ServiceTopology, MonitoredElement>() {
							@Override
							public void mapAtoB(ServiceTopology topology, MonitoredElement element,
									MappingContext context) {
								// set level
								element.setLevel(decideLevel(topology));

								// children
								List<MonitoredElement> list = new ArrayList<>();
								element.setContainedElements(list);

								for (ServiceTopology child : topology.getServiceTopologies()) {
									list.add(facade.map(child, MonitoredElement.class));
								}

								for (ServiceUnit unit : topology.getServiceUnits()) {
									list.add(facade.map(unit, MonitoredElement.class));
								}
							}
						})
				.register();

		mapperFactory.classMap(ServiceUnit.class, MonitoredElement.class)
				.field("id", "id")
				.field("name", "name")
				.customize(
						new CustomMapper<ServiceUnit, MonitoredElement>() {
							@Override
							public void mapAtoB(ServiceUnit unit, MonitoredElement element, MappingContext context) {
								// set level
								element.setLevel(decideLevel(unit));

								// set VMs

							}
						})

				.register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

	public static MonitoredElementLevel decideLevel(ServicePart part) {

		if (part instanceof CloudService) {
			return MonitoredElementLevel.SERVICE;
		} else if (part instanceof ServiceTopology) {
			return MonitoredElementLevel.SERVICE_TOPOLOGY;
		} else if (part instanceof ServiceUnit) {
			return MonitoredElementLevel.SERVICE_UNIT;
		} else {
			throw new UnsupportedOperationException();
		}

	}

}
