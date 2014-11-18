package at.ac.tuwien.dsg.comot.cs.test.mapping;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.UtilsMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;

public class RsyblMappingTest extends AbstractTest {

	@Autowired
	protected RsyblOrika orika;
	@Autowired
	protected RsyblMapper mapper;

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		CloudServiceXML rsybl = mapper.extractRsybl(serviceForMapping);
		log.info("rsybl {}", UtilsMapper.asString(rsybl));

	}

	@Test
	public void orikaTest() throws JAXBException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		CloudServiceXML xml = orika.get().map(serviceForMapping, CloudServiceXML.class);
		log.info("tosca1 {}", UtilsMapper.asString(xml));

	}

}
