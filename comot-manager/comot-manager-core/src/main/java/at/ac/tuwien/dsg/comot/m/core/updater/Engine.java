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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.core.updater.Node.State;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

@Component
public class Engine {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Navigator navNeu;
	protected Navigator navOld;
	protected Set<ServiceUnit> forceUpdate;

	@Autowired
	protected DeploymentWrapper deployment;

	public Engine() {

	}

	public Engine(CloudService old, CloudService neu, Set<ServiceUnit> forceUpdate) {
		setUp(old, neu, forceUpdate);
	}

	public void setUp(CloudService old, CloudService neu, Set<ServiceUnit> forceUpdate) {
		navNeu = new Navigator(neu);
		navOld = new Navigator(old);

		this.forceUpdate = forceUpdate;
	}

	public void update() {

		Set<ServiceUnit> roots = new HashSet<>();

		for (ServiceUnit unit : navNeu.getAllUnits()) {
			if (unit.getOsuInstance().getOsu().getType().equals(OsuType.OS.toString())
					&& navOld.getUnit(unit.getId()) != null) {
				roots.add(unit);
			}
		}

		for (ServiceUnit unit : roots) {

			Tree tree = new Tree(unit, navNeu, navOld);
			Node rootNode = tree.getRoot();

			markNodeRecursive(rootNode);

			if (rootNode.isDeploy() || rootNode.isMigrate()) {
				deployment.createVM(rootNode);
			}

			for (Node leaf : tree.getLeafs()) {
				preMigrateRecursive(leaf);
			}

			migrateRecursive(rootNode);

			for (Node leaf : tree.getLeafs()) {
				postMigrateRecursive(leaf);
			}

		}

	}

	protected void preMigrateRecursive(Node node) {

		boolean all = true;

		if (node.isMigrate()) {
			deployment.preMigrate(node);
		}

		if (node.getParent() != null) {
			for (Node someLeaf : node.getParent().getChildren()) {
				if (!someLeaf.getState().equals(State.PRE_MIGRATE_FINISHED) &&
						!someLeaf.getState().equals(State.NONE)) {
					all = false;
				}
			}
			if (all) {
				preMigrateRecursive(node.getParent());
			}
		}

	}

	protected void migrateRecursive(Node node) {

		if (node.isMigrate()) {
			deployment.migrate(node);
		} else if (node.isDeploy()) {
			deployment.deploy(node);
		}

		for (Node child : node.getChildren()) {
			migrateRecursive(child);
		}

	}

	protected void postMigrateRecursive(Node node) {

		boolean all = true;

		if (node.isMigrate()) {
			deployment.postMigrate(node);
		}

		if (node.getParent() != null) {
			for (Node someLeaf : node.getParent().getChildren()) {
				if (!someLeaf.getState().equals(State.POST_MIGRATE_FINISHED) &&
						!someLeaf.getState().equals(State.DEPLOY_FINISHED)) {
					all = false;
				}
			}
			if (all) {
				postMigrateRecursive(node.getParent());
			}
		}

	}

	protected void markNodeRecursive(Node node) {

		if (node.isInOldService()) {

			if (changed(node)) {
				node.setMigrate(true);
			}

			if (node.getParent() != null) {
				if (node.getParent().isDeploy() || node.getParent().isMigrate()) {
					node.setMigrate(true);
				}
			}
			// TODO: maybe we need to also check if the path is same

		} else {
			node.setDeploy(true);
		}

		for (Node child : node.getChildren()) {

			markNodeRecursive(child);
		}
	}

	protected boolean changed(Node node) {

		// TODO make work with offered service units

		ServiceUnit unitNew = node.getUnit();

		if (forceUpdate.contains(unitNew)) {
			return true;
		}

		//
		// ServiceUnit unitOld = navOld.getUnit(unitNew.getId());
		//
		//
		//
		// if(unitNew.getDeploymentArtifactsList().get(0).getUrisList().get(0)
		// .equals(unitOld.getDeploymentArtifactsList().get(0).getUrisList().get(0))){
		//
		// }

		return false;
	}
}
