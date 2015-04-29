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

import java.util.Map;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public interface DeploymentClient extends ElasticPlatformServiceClient {

	public CloudService deploy(
			CloudService service) throws EpsException, ComotException;

	public CloudService deploy(
			String service) throws EpsException, ComotException;

	public void undeploy(
			String serviceId) throws EpsException;

	public void spawn(
			String serviceId,
			String topologyId,
			String nodeId,
			int instanceCount) throws EpsException;

	public void destroy(
			String serviceId,
			String topologyId,
			String nodeId,
			int instanceId) throws EpsException;

	public CloudService refreshStatus(
			CloudService service) throws EpsException, ComotException;

	public CloudService refreshStatus(
			Map<String, String> map,
			CloudService service) throws EpsException, ComotException;

	public boolean isManaged(String serviceId) throws EpsException;

	public CloudService getService(String serviceId) throws EpsException, ComotException;

	boolean isRunning(String serviceID) throws EpsException, ComotException;

}
