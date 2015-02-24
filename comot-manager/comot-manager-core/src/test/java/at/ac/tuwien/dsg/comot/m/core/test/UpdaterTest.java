package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.updater.Engine;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;

public class UpdaterTest extends AbstractTest {

	@Autowired
	public Engine engine;

	public static final String EP_UNIT_ID = "EventProcessing";
	public static final String SEED_OS_UNIT_ID = "os_OF_CassandraSeed";

	@Test
	public void dostuff() throws CoreServiceException, ComotException, JAXBException, IOException,
			ClassNotFoundException {

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "daas_m2m_fromSalsa.xml");

		CloudService serviceOld = mapperTosca.createModel(def);
		CloudService serviceNew = (CloudService) Utils.deepCopy(serviceOld);

		log.info("service {}", Utils.asXmlString(serviceOld));

		Navigator nav = new Navigator(serviceNew);
		Set<ServiceUnit> forceUpdate = new HashSet<>();

		forceUpdate.add(nav.getUnit(EP_UNIT_ID));
		forceUpdate.add(nav.getUnit(SEED_OS_UNIT_ID));

		engine.setUp(serviceOld, serviceNew, forceUpdate);
		engine.update();

	}

}
