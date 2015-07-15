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
package at.ac.tuwien.dsg.comot.m.adapter.general;

import javax.xml.bind.JAXBException;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public interface IManager {

	void sendLifeCycleEvent(String serviceId, String groupId, Action action) throws JAXBException;

	void sendLifeCycleEvent(String serviceId, String groupId, Action action, String parentId, ServiceEntity entity)
			throws JAXBException;

	void sendLifeCycleEvent(String serviceId, String groupId, Action action, String parentId, UnitInstance instance)
			throws JAXBException;

	void sendCustomEvent(String serviceId, String groupId, String eventName, String epsId, String message)
			throws JAXBException;

	void sendCustomEvent(String serviceId, String groupId, String eventName, String epsId, String message,
			String correlationId) throws JAXBException;

	void sendExceptionEvent(String instanceId, String eventCauseId, Exception e) throws JAXBException;

	void stop();

	String logId();

	void sendLifeCycle(LifeCycleEvent event) throws JAXBException;

	void sendCustomEvent(CustomEvent event) throws JAXBException;

}
