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
package at.ac.tuwien.dsg.comot.m.core.updater;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public interface DeploymentService {

	/*
	 * Update tosca without redeployment. Only used to update actions and scripts.
	 */
	public void updateDescription(CloudService service);

	public void createVMCore(Node node);

	public void deploy(Node node);

	public void remove(Node node);

	public void migrate(Node node);

	public void preMigrate(Node node);

	public void postMigrate(Node node);

	public void start(Node node);

	public void stop(Node node);

	public void reconfigure(Node node);

}
