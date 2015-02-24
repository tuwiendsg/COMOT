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
