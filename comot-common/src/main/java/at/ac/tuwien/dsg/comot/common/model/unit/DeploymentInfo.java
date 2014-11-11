package at.ac.tuwien.dsg.comot.common.model.unit;

import java.util.List;


public class DeploymentInfo {
	
	protected String defaultImage;
	protected String defaultFlavor;
	protected String serviceUnitID;
	protected List<AssociatedVM> associatedVMs;
	protected List<ElasticityCapability> elasticityCapabilities;
	
	
	
	public String getDefaultImage() {
		return defaultImage;
	}
	public void setDefaultImage(String defaultImage) {
		this.defaultImage = defaultImage;
	}
	public String getDefaultFlavor() {
		return defaultFlavor;
	}
	public void setDefaultFlavor(String defaultFlavor) {
		this.defaultFlavor = defaultFlavor;
	}
	public String getServiceUnitID() {
		return serviceUnitID;
	}
	public void setServiceUnitID(String serviceUnitID) {
		this.serviceUnitID = serviceUnitID;
	}
	public List<AssociatedVM> getAssociatedVMs() {
		return associatedVMs;
	}
	public void setAssociatedVMs(List<AssociatedVM> associatedVMs) {
		this.associatedVMs = associatedVMs;
	}
	public List<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}
	public void setElasticityCapabilities(List<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}
	
	
	

}
