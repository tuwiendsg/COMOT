package at.ac.tuwien.dsg.comot.m.cs.mapper.orika;

import java.util.Set;

import javax.annotation.PostConstruct;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.SYBLDirective;

@Component
public class RsyblOrika {

	protected final Logger log = LoggerFactory.getLogger(RsyblOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		ConverterFactory converterFactory = mapperFactory.getConverterFactory();

		converterFactory.registerConverter(new SyblConverter());

		mapperFactory.classMap(CloudService.class, CloudServiceXML.class)
				.field("serviceTopologies", "serviceTopology")
				.fieldAToB("directives", "SYBLDirective")
				.byDefault()
				.register();

		mapperFactory.classMap(ServiceTopology.class, at.ac.tuwien.dsg.comot.rsybl.ServiceTopology.class)
				.field("serviceTopologies", "serviceTopology")
				// .field("serviceUnits", "serviceUnit")
				.fieldAToB("directives", "SYBLDirective")
				.byDefault()
				.customize(
						new CustomMapper<ServiceTopology, at.ac.tuwien.dsg.comot.rsybl.ServiceTopology>() {
							@Override
							public void mapAtoB(ServiceTopology topology,
									at.ac.tuwien.dsg.comot.rsybl.ServiceTopology rTopology, MappingContext context) {

								for (ServiceUnit unit : topology.getServiceUnits()) {
									if (Navigator.isTrueServiceUnit(unit, topology.getServiceUnits())) {
										rTopology.withServiceUnit(facade.map(unit,
												at.ac.tuwien.dsg.comot.rsybl.ServiceUnit.class));
									}
								}

								// TODO how to mapp relationships
							}
						})
				.register();

		mapperFactory.classMap(ServiceUnit.class, at.ac.tuwien.dsg.comot.rsybl.ServiceUnit.class)
				.fieldAToB("directives", "SYBLDirective")
				.exclude("properties")
				.byDefault()
				.register();

		facade = mapperFactory.getMapperFacade();
	}

	@SuppressWarnings("rawtypes")
	class SyblConverter extends CustomConverter<Set, SYBLDirective> {
		@Override
		public SYBLDirective convert(Set source, Type<? extends SYBLDirective> destinationType) {

			SyblDirective directive;
			SYBLDirective rDirecitve = new SYBLDirective();

			for (Object obj : source) {
				directive = (SyblDirective) obj;

				switch (directive.getType()) {
				case CONSTRAINT:
					rDirecitve.setConstraints(directive.getDirective());
					break;
				case STRATEGY:
					rDirecitve.setStrategies(directive.getDirective());
					break;
				case MONITORING:
					rDirecitve.setMonitoring(directive.getDirective());
					break;
				case PRIORIIES:
					rDirecitve.setPriorities(directive.getDirective());
					break;
				default:
					throw new IllegalArgumentException("Unexpected value " + directive.getType()
							+ " of enum at.ac.tuwien.dsg.comot.common.model.type.DirectiveType");
				}
			}
			return rDirecitve;
		}
	}

	public MapperFacade get() {
		return facade;
	}

}