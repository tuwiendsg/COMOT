package at.ac.tuwien.dsg.comot.m.core.model;

import java.util.Comparator;

public class ServiceEntityComparator implements Comparator<ServiceEntity> {
	@Override
	public int compare(ServiceEntity o1, ServiceEntity o2) {
		return o1.getId().compareToIgnoreCase(o2.getId());
	}

}
