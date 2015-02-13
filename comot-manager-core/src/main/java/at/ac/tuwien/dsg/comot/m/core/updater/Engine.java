package at.ac.tuwien.dsg.comot.m.core.updater;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.m.core.updater.Node.State;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.NodeType;

@Component
public class Engine {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Navigator navNeu;
	protected Navigator navOld;
	protected Set<ServiceUnit> forceUpdate;

	@Autowired
	protected DeploymentService deployment;

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

		Set<ServiceUnit> added = new HashSet<>();
		Set<ServiceUnit> deleted = new HashSet<>();

		Set<ServiceUnit> roots = new HashSet<>();

		for (ServiceUnit unit : navNeu.getAllUnits()) {
			if (unit.getType().equals(NodeType.OS) && navOld.getUnit(unit.getId()) != null) {
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
