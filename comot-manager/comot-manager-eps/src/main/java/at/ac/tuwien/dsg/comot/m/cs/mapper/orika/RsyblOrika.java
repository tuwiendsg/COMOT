/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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

import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.SYBLDirective;

@Component
public class RsyblOrika {

	public static final String SEPARATOR_ID = ":";
	public static final String SEPARATOR_DIRECTIVES = ";";

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
				String strDir = directive.getId() + SEPARATOR_ID + directive.getDirective();
				String prevDir;

				if (!strDir.trim().endsWith(SEPARATOR_DIRECTIVES)) {
					strDir += SEPARATOR_DIRECTIVES;
				}

				switch (directive.getType()) {

				case CONSTRAINT:
					prevDir = rDirecitve.getConstraints();
					rDirecitve.setConstraints(((prevDir == null) ? "" : prevDir) + strDir);
					break;
				case STRATEGY:
					prevDir = rDirecitve.getStrategies();
					rDirecitve.setStrategies(((prevDir == null) ? "" : prevDir) + strDir);
					break;
				case MONITORING:
					prevDir = rDirecitve.getMonitoring();
					rDirecitve.setMonitoring(((prevDir == null) ? "" : prevDir) + strDir);
					break;
				case PRIORITY:
					prevDir = rDirecitve.getPriorities();
					rDirecitve.setPriorities(((prevDir == null) ? "" : prevDir) + strDir);
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
