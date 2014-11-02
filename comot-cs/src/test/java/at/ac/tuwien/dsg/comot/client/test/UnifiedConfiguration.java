package at.ac.tuwien.dsg.comot.client.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class UnifiedConfiguration {
	
	 @XmlElement(name = "CompositionRulesConfiguration")
	protected CompositionRulesConfiguration mcr;
	
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
	
	

}
