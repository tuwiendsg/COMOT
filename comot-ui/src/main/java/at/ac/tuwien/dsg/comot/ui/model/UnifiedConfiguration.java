package at.ac.tuwien.dsg.comot.ui.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ElasticCloudService")
public class UnifiedConfiguration {

	@XmlElement(name = "Tosca", required = true)
	protected Definitions definition;
	@XmlElement(name = "MetricCompositionRules", required = false)
	protected CompositionRulesConfiguration mcr;
	@XmlElement(name = "ElasticityCapabilitiesEffects", required = false)
	protected String effects;

	public CompositionRulesConfiguration getMcr() {
		return mcr;
	}

	public void setMcr(CompositionRulesConfiguration mcr) {
		this.mcr = mcr;
	}

	public String getEffects() {
		return effects;
	}

	public void setEffects(String effects) {
		this.effects = effects;
	}

	public Definitions getDefinition() {
		return definition;
	}

	public void setDefinition(Definitions definition) {
		this.definition = definition;
	}

}
