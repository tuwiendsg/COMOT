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

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement.MonitoredElementLevel;

@Component
public class MelaOrika {

	private static final Logger LOG = LoggerFactory.getLogger(MelaOrika.class);

	protected MapperFacade facade;

	@PostConstruct
	public void build() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

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
							}
						})

				.register();

		facade = mapperFactory.getMapperFacade();
	}

	public MapperFacade get() {
		return facade;
	}

	public static MonitoredElementLevel decideLevel(ServiceEntity part) {

		if (part instanceof CloudService) {
			return MonitoredElementLevel.SERVICE;
		} else if (part instanceof ServiceTopology) {
			return MonitoredElementLevel.SERVICE_TOPOLOGY;
		} else if (part instanceof ServiceUnit) {
			return MonitoredElementLevel.SERVICE_UNIT;
		} else {
			throw new IllegalArgumentException(
					"Unexpected inheritance of at.ac.tuwien.dsg.comot.common.model.structure.ServicePart");
		}

	}

}
