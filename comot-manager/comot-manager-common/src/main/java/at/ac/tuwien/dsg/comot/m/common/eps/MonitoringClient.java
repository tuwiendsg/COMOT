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

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface MonitoringClient extends ElasticPlatformServiceClient {

	// start
	public void startMonitoring(
			CloudService sevice) throws EpsException, ComotException;

	// stop
	public void stopMonitoring(
			String serviceId) throws EpsException;

	// update
	public void updateService(
			String serviceId,
			CloudService sevice) throws EpsException, ComotException;

	public void setMcr(
			String serviceId,
			CompositionRulesConfiguration mcr) throws EpsException;

	// get
	public ElementMonitoring getMonitoringData(
			String serviceId) throws EpsException, ComotException;

	public CompositionRulesConfiguration getMcr(
			String serviceId) throws EpsException;

	public List<String> listAllServices() throws EpsException;

	boolean isMonitored(String instanceId) throws EpsException;

	// public void getServiceDescription(String serviceId) throws CoreServiceException;
}
