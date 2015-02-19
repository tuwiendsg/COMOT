package at.ac.tuwien.dsg.comot.m.recorder.cs;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;

public class ManagedService {

	protected String serviceId;
	protected DeploymentClient deployment;
	protected ControlClient control;
	protected MonitoringClient monitoring;

	public ManagedService(String serviceId, DeploymentClient deployment, ControlClient control,
			MonitoringClient monitoring) {
		super();
		this.serviceId = serviceId;
		this.deployment = deployment;
		this.control = control;
		this.monitoring = monitoring;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public DeploymentClient getDeployment() {
		return deployment;
	}

	public void setDeployment(DeploymentClient deployment) {
		this.deployment = deployment;
	}

	public ControlClient getControl() {
		return control;
	}

	public void setControl(ControlClient control) {
		this.control = control;
	}

	public MonitoringClient getMonitoring() {
		return monitoring;
	}

	public void setMonitoring(MonitoringClient monitoring) {
		this.monitoring = monitoring;
	}

}
