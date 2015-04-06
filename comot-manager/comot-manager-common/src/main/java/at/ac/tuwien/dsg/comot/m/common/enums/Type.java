package at.ac.tuwien.dsg.comot.m.common.enums;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public enum Type {
	SERVICE, TOPOLOGY, UNIT, INSTANCE;

	public static Type decide(Object obj) {

		if (obj instanceof CloudService) {
			return SERVICE;
		} else if (obj instanceof ServiceTopology) {
			return TOPOLOGY;
		} else if (obj instanceof ServiceUnit) {
			return UNIT;
		} else if (obj instanceof UnitInstance) {
			return INSTANCE;
		}

		return null;
	}
}
