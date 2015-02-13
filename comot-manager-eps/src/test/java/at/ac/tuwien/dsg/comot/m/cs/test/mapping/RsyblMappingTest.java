package at.ac.tuwien.dsg.comot.m.cs.test.mapping;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class RsyblMappingTest extends AbstractTest {

	@Autowired
	protected RsyblOrika orika;
	@Autowired
	protected RsyblMapper mapper;

	protected CloudService serviceForMapping;

	@Before
	public void startup() {
		serviceForMapping = STemplates.fullServiceWithoutInstances();
	}

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException {

		log.info("original {}", Utils.asString(serviceForMapping));

		CloudServiceXML rsybl = mapper.extractRsybl(serviceForMapping);
		log.info("rsybl {}", UtilsCs.asString(rsybl));

	}

	@Test
	public void orikaTest() throws JAXBException {

		log.info("original {}", Utils.asString(serviceForMapping));

		CloudServiceXML xml = orika.get().map(serviceForMapping, CloudServiceXML.class);
		log.info("tosca1 {}", UtilsCs.asString(xml));

	}

}
