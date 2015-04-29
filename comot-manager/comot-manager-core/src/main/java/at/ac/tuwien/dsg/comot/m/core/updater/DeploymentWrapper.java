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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.core.updater.Node.State;

@Component
public class DeploymentWrapper {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected DeploymentService deployment;

	public void createVM(Node node) {

		// TODO
	}

	public void deploy(Node node) {
		node.setState(State.DEPLOY_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		deployment.deploy(node);

		node.setState(State.DEPLOY_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void remove(Node node) {

	}

	public void migrate(Node node) {
		node.setState(State.MIGRATE_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		deployment.migrate(node);

		node.setState(State.MIGRATE_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void preMigrate(Node node) {

		node.setState(State.PRE_MIGRATE_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		deployment.preMigrate(node);

		node.setState(State.PRE_MIGRATE_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void postMigrate(Node node) {
		node.setState(State.POST_MIGRATE_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		deployment.postMigrate(node);

		node.setState(State.POST_MIGRATE_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void start(Node node) {

	}

	public void stop(Node node) {

	}

	public void reconfigure(Node node) {

	}

}
