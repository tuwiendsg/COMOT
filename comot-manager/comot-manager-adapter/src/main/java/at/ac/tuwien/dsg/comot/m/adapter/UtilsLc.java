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
package at.ac.tuwien.dsg.comot.m.adapter;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.CharEncoding;
import org.springframework.amqp.core.Message;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

public class UtilsLc {

	private UtilsLc() {

	}

	public static void removeProviderInfo(CloudService service) {
		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setOsuInstance(null);
		}
	}

	public static void removeProviderInfoExceptType(CloudService service) {

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			OfferedServiceUnit osu = unit.getOsuInstance().getOsu();
			osu.setResources(null);
			osu.setCostFunctions(null);
			osu.setPrimitiveOperations(null);
			osu.setQualities(null);
		}

		service.setSupport(null);

	}

	public static ComotMessage comotMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		return Utils.asObjectFromJson(new String(message.getBody(), CharEncoding.UTF_8), ComotMessage.class);
	}

	public static StateMessage stateMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		return Utils.asObjectFromJson(new String(message.getBody(), CharEncoding.UTF_8), StateMessage.class);
	}

	public static AbstractEvent abstractEvent(Message message) throws UnsupportedEncodingException, JAXBException {
		return Utils.asObjectFromJson(new String(message.getBody(), CharEncoding.UTF_8), AbstractEvent.class);
	}

}
