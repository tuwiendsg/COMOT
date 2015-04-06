package at.ac.tuwien.dsg.comot.m.ui.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ServiceInstanceUi {

	String instanceId;
	CloudService service; // complete information
	Map<String, Transition> transitions;

	public ServiceInstanceUi() {

	}

	public ServiceInstanceUi(String instanceId, CloudService service, Map<String, Transition> transitions) {
		super();
		this.instanceId = instanceId;
		this.service = service;
		this.transitions = transitions;
	}

}
