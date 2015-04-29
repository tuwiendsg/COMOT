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

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;

public class Node {

	protected ServiceUnit unit;

	protected Node parent;
	protected Set<Node> children;

	protected Navigator nav;
	protected Navigator navOld;

	protected boolean deploy;
	protected boolean migrate;
	protected State state;

	enum State {
		NONE,
		PRE_MIGRATE_STARTED, PRE_MIGRATE_FINISHED,
		MIGRATE_STARTED, MIGRATE_FINISHED,
		POST_MIGRATE_STARTED, POST_MIGRATE_FINISHED,
		DEPLOY_STARTED, DEPLOY_FINISHED,
		REMOVE_STARTED, REMOVE_FINISHED
	}

	public Node(ServiceUnit unit, Node parent, Navigator nav, Navigator navOld) {
		super();
		this.unit = unit;
		this.parent = parent;
		this.nav = nav;
		this.navOld = navOld;
		this.children = new HashSet<>();
		this.state = State.NONE;

		for (ServiceUnit child : nav.getHostedOn(unit)) {
			this.children.add(new Node(child, this, nav, navOld));
		}

	}

	boolean isVM() {
		return parent == null;
	}

	public boolean isInOldService() {
		return navOld.getUnit(unit.getId()) != null;
	}

	public Set<Node> getSubtree() {
		Set<Node> subtree = new HashSet<>();

		subtree.add(this);
		subtree.addAll(children);

		for (Node child : children) {
			subtree.addAll(child.getSubtree());
		}
		return subtree;
	}

	public boolean allChildrenIn(State state) {

		for (Node child : children) {
			if (!child.getState().equals(state)) {
				return false;
			}
		}

		return true;
	}

	// GENERATED

	public ServiceUnit getUnit() {
		return unit;
	}

	public void setUnit(ServiceUnit unit) {
		this.unit = unit;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Set<Node> getChildren() {
		return children;
	}

	public void setChildren(Set<Node> children) {
		this.children = children;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isDeploy() {
		return deploy;
	}

	public void setDeploy(boolean create) {
		this.deploy = create;
	}

	public boolean isMigrate() {
		return migrate;
	}

	public void setMigrate(boolean migrate) {
		this.migrate = migrate;
	}

	@Override
	public String toString() {
		return "Node [unit=" + unit.getId() + ", deploy=" + deploy + ", migrate=" + migrate + ", state=" + state + "]";
	}

}
