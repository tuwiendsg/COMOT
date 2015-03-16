package at.ac.tuwien.dsg.comot.m.ui.model;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ServiceAndInstances {

	String serviceId;
	String[] instanceId;

	public ServiceAndInstances() {

	}

	public ServiceAndInstances(String serviceId, List<String> instanceIds) {
		super();
		this.serviceId = serviceId;

		instanceId = new String[instanceIds.size()];
		for (int i = 0; i < instanceIds.size(); i++) {
			instanceId[i] = instanceIds.get(i);
		}
	}

	@Override
	public String toString() {
		return "ServiceAndInstances [serviceId=" + serviceId + ", instanceId=" + Arrays.toString(instanceId) + "]";
	}

}
