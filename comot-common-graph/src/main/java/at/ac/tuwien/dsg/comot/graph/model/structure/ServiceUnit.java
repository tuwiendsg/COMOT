package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;

public class ServiceUnit extends ServicePart {

	private static final long serialVersionUID = -1213074714671448573L;

	protected Set<ElasticityCapability> elasticityCapabilities = new HashSet<>();

	protected Set<CodeRegion> codeRegions = new HashSet<>();
	protected StackNode node;

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
		this.node = node;
	}

	public ServiceUnit(StackNode node, Set<SyblDirective> directives) {
		super(node.getId(), directives);
		this.node = node;
	}

	public void addCodeRegion(CodeRegion region) {
		if (codeRegions == null) {
			codeRegions = new HashSet<>();
		}
		codeRegions.add(region);
	}

	// GENERATED METHODS

	public Set<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}

	public void setElasticityCapabilities(Set<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}

	public Set<CodeRegion> getCodeRegions() {
		return codeRegions;
	}

	public void setCodeRegions(Set<CodeRegion> codeRegions) {
		this.codeRegions = codeRegions;
	}

	public StackNode getNode() {
		return node;
	}

	public void setNode(StackNode node) {
		this.node = node;
	}

}
