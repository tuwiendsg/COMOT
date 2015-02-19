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
