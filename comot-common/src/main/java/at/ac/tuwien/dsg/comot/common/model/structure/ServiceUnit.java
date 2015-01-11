package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.unit.ElasticityCapability;

public class ServiceUnit extends ServicePart {

	private static final long serialVersionUID = -1213074714671448573L;

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

	public List<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}

	public void setElasticityCapabilities(List<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}

}