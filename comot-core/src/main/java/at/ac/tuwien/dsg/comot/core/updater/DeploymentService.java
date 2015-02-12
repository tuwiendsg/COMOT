package at.ac.tuwien.dsg.comot.core.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.core.updater.Node.State;

@Component
public abstract class DeploymentService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void createVM(Node node) {

		// TODO
	}

	public void deploy(Node node) {
		node.setState(State.DEPLOY_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		deployCore(node);

		node.setState(State.DEPLOY_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void remove(Node node) {

	}

	public void migrate(Node node) {
		node.setState(State.MIGRATE_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		migrateCore(node);

		node.setState(State.MIGRATE_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void preMigrate(Node node) {

		node.setState(State.PRE_MIGRATE_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		preMigrateCore(node);

		node.setState(State.PRE_MIGRATE_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void postMigrate(Node node) {
		node.setState(State.POST_MIGRATE_STARTED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());

		postMigrateCore(node);

		node.setState(State.POST_MIGRATE_FINISHED);
		log.info("node={} state={}", node.getUnit().getId(), node.getState());
	}

	public void start(Node node) {

	}

	public void stop(Node node) {

	}

	public void reconfigure(Node node) {

	}

	public abstract void createVMCore(Node node);

	public abstract void deployCore(Node node);

	public abstract void removeCore(Node node);

	public abstract void migrateCore(Node node);

	public abstract void preMigrateCore(Node node);

	public abstract void postMigrateCore(Node node);

	public abstract void startCore(Node node);

	public abstract void stopCore(Node node);

	public abstract void reconfigureCore(Node node);

}
