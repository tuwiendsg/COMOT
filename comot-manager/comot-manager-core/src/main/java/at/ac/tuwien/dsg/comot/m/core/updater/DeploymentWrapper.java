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
