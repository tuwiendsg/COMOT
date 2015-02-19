package at.ac.tuwien.dsg.comot.model.runtime;

import java.util.Set;

public class ServiceInstance {
	
	protected Set<UnitInstance> unitInstances;

	public Set<UnitInstance> getUnitInstances() {
		return unitInstances;
	}

	public void setUnitInstances(Set<UnitInstance> unitInstances) {
		this.unitInstances = unitInstances;
	}
	
	

}
