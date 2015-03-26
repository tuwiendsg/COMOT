package at.ac.tuwien.dsg.comot.m.adapter;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.ServiceInstance;

public class UtilsLc {

	private static final Logger log = LoggerFactory.getLogger(UtilsLc.class);

	public static CloudService removeProviderInfo(CloudService service) {
		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setOsu(null);
		}
		for (ServiceInstance instance : service.getInstances()) {
			instance.setSupport(new HashSet<OfferedServiceUnit>());
		}

		return service;
	}

	public static ComotMessage comotMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		return Utils.asObjectFromJson(new String(message.getBody(), "UTF-8"), ComotMessage.class);
	}

	public static StateMessage stateMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		return Utils.asObjectFromJson(new String(message.getBody(), "UTF-8"), StateMessage.class);
	}

	public static AbstractEvent abstractEvent(Message message) throws UnsupportedEncodingException, JAXBException {
		return Utils.asObjectFromJson(new String(message.getBody(), "UTF-8"), AbstractEvent.class);
	}

}
