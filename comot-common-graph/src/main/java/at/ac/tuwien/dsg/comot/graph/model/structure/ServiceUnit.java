package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.unit.ElasticityCapability;

public class ServiceUnit extends ServicePart {

	private static final long serialVersionUID = -1213074714671448573L;

	protected Set<ElasticityCapability> elasticityCapabilities = new HashSet<>();

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

	public ServiceUnit(StackNode node, Set<SyblDirective> directives) {
		super(node.getId(), directives);
	}

	public Set<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}

	public void setElasticityCapabilities(Set<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}

}
