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
package at.ac.tuwien.dsg.comot.m.common.eps;

import java.util.List;

import javax.xml.bind.JAXBException;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface ControlClient extends ElasticPlatformServiceClient {

	public void sendInitialConfig(
			CloudService service) throws EpsException, JAXBException;

	public void createMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException;

	public void createEffects(String serviceId, String effectsJSON)
			throws EpsException;

	public void startControl(
			String serviceId) throws EpsException;

	public void stopControl(
			String serviceId) throws EpsException;

	public void updateEffects(String serviceId, String effectsJSON) throws EpsException;

	public void updateMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException;

	public void updateService(CloudService service) throws EpsException, Exception;

	public List<String> listAllServices() throws EpsException;

	public void removeService(String serviceId) throws EpsException;

	public boolean isControlled(String instanceId) throws EpsException;

	public void registerForEvents(String serviceId, ControlEventsListener listener) throws Exception;

	public void removeListener(String serviceId);
}
