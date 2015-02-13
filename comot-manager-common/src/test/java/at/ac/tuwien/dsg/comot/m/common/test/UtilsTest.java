package at.ac.tuwien.dsg.comot.m.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;

public class UtilsTest {

	private final static Logger log = LoggerFactory.getLogger(UtilsTest.class);

	public static final String TEST_FILE_BASE = "./../resources/test/";

	public static void sleepInfinit() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void sleepSeconds(int seconds) {
		try {
			log.debug("Waiting {} seconds", seconds);
			Thread.sleep(seconds * 1000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static ServiceUnit getServiceUnit(CloudService service, String id) {

		for (ServiceTopology topo : Navigator.getAllTopologies(service)) {
			for (ServiceUnit unit : topo.getServiceUnits()) {
				if (unit.getId().equals(id)) {
					return unit;
				}
			}
		}

		return null;

	}

}
