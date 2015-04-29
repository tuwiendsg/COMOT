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

public class Tree {

	protected Node root;
	protected Set<Node> allNodes;
	protected Set<Node> leafs;

	public Tree(ServiceUnit unit, Navigator nav, Navigator navOld) {

		root = new Node(unit, null, nav, navOld);
		allNodes = root.getSubtree();
		leafs = new HashSet<Node>();

		for (Node node : allNodes) {

			if (node.getChildren().isEmpty()) {
				leafs.add(node);
			}
		}
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public Set<Node> getAllNodes() {
		return allNodes;
	}

	public void setAllNodes(Set<Node> allNodes) {
		this.allNodes = allNodes;
	}

	public Set<Node> getLeafs() {
		return leafs;
	}

	public void setLeafs(Set<Node> leafs) {
		this.leafs = leafs;
	}

}
