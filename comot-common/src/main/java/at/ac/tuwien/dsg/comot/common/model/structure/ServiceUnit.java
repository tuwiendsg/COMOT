package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.List;



import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.unit.DeploymentInfo;
import at.ac.tuwien.dsg.comot.common.model.unit.ElasticityCapability;

public class ServiceUnit extends ServicePart {

	protected DeploymentInfo deploymentInfo;
	protected List<ElasticityCapability> elasticityCapabilities;

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

	public List<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}

	public void setElasticityCapabilities(List<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}

}
