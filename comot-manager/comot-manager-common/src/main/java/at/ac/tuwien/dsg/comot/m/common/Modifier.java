package at.ac.tuwien.dsg.comot.m.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;

public class Modifier {

	protected static final Logger log = LoggerFactory.getLogger(Modifier.class);

	public static void replaceSyblDirectives(CloudService from, CloudService to) {

		Navigator navTo = new Navigator(to);
		Navigator navFrom = new Navigator(from);

		for (ServiceEntity entity : navTo.getAllServiceEntities()) {
			if (navFrom.getManaged(entity.getId()) != null) {

				ServiceEntity temp = (ServiceEntity) navFrom.getManaged(entity.getId());
				entity.setDirectives(temp.getDirectives());
			}
		}

	}

}
