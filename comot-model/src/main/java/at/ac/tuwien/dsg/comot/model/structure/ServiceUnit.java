package at.ac.tuwien.dsg.comot.model.structure;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;

import at.ac.tuwien.dsg.comot.model.ElasticityCapability;

public class ServiceUnit extends ServicePart {

	private static final long serialVersionUID = -1213074714671448573L;

	@XmlElementWrapper(name = "ElasticityCapabilities")
	@XmlElement(name = "Capability")
	protected Set<ElasticityCapability> elasticityCapabilities = new HashSet<>();
	@XmlElementWrapper(name = "RodeRegions")
	@XmlElement(name = "Region")
	protected Set<CodeRegion> codeRegions = new HashSet<>();
	@XmlIDREF
	@XmlAttribute
	protected StackNode node;

	public ServiceUnit() {
		super();
	}

	public ServiceUnit(String id, StackNode node) {
		this.node = node;
		this.id = id;
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
