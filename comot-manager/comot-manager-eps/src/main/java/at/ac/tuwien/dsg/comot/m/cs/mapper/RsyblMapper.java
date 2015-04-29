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
package at.ac.tuwien.dsg.comot.m.cs.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;

@Component
public class RsyblMapper {

	protected final Logger log = LoggerFactory.getLogger(RsyblMapper.class);

	@Autowired
	protected RsyblOrika mapper;

	public CloudServiceXML extractRsybl(CloudService cloudService) {

		// cloudService = (CloudService) UtilsMapper.deepCopy(cloudService);
		// Navigator navigator = new Navigator(cloudService);

		// TODO check with Georgiana if SW units should really be removed
		// ignore SOFTWARE nodes
		// for (StackNode unit : navigator.getAllNodes()) {
		// if (unit.getType().equals(SwType.SOFTWARE)) {
		// navigator.getParentTopology(unit.getId()).getServiceUnits().remove(unit);
		// }
		// }

		CloudServiceXML serviceXml = mapper.get().map(cloudService, CloudServiceXML.class);

		// log.trace("Final mapping: {}", );

		return serviceXml;
	}
}
