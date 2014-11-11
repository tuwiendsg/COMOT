package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.List;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.rSYBL.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.rSYBL.deploymentDescription.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.unit.DeploymentInfo;

public class ServiceUnit extends ServicePart {


	protected DeploymentInfo deploymentInfo;
	
	public ServiceUnit() {
		super();
	}

	/**
	 * Unit shares ID with the node
	 * 
	 * @param node
	 */
	public ServiceUnit(StackNode node) {
		super(node.getId());
	}

	public ServiceUnit(StackNode node, List<SyblDirective> directives) {
		super(node.getId(), directives);
	}

	public DeploymentInfo getDeploymentInfo() {
		return deploymentInfo;
	}

	public void setDeploymentInfo(DeploymentInfo deploymentInfo) {
		this.deploymentInfo = deploymentInfo;
	}
	
	

}
