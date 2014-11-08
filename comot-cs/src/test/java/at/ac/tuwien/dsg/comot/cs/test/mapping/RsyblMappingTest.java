package at.ac.tuwien.dsg.comot.cs.test.mapping;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.UtilsMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.ObjectFactory;

public class RsyblMappingTest extends AbstractTest {

	@Autowired
	protected RsyblOrika orika;
	@Autowired
	protected RsyblMapper mapper;

	@Test
	public void mapperTest() throws JAXBException {

		// log.info("original {}", Utils.asJsonString(serviceForMapping));
		//
		// Definitions tosca1 = myMapper.toTosca(serviceForMapping);
		// log.info("tosca1 {}", UtilsMapper.asString(tosca1));
		//
		// CloudService service2 = myMapper.toModel(tosca1);
		// log.info("service2 {}", Utils.asJsonString(service2));
		//
		// Definitions tosca2 = myMapper.toTosca(service2);
		// log.info("tosca2 {}", UtilsMapper.asString(tosca2));
		//
		// CloudService service3 = myMapper.toModel(tosca1);
		// log.info("service3 {}", Utils.asJsonString(service3));

	}

	@Test
	public void orikaTest() throws JAXBException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		CloudServiceXML xml = orika.get().map(serviceForMapping, CloudServiceXML.class);
		log.info("tosca1 {}", UtilsMapper.asString(xml));

	}

	@Test
	public void testProvidedClasses() throws JAXBException {

		CloudServiceXML cloudServiceXML = new CloudServiceXML();
		ObjectFactory factory = new ObjectFactory();

		factory.createCloudService(cloudServiceXML);
		log.info("cloudServiceXML: {}", UtilsMapper.asString(cloudServiceXML));

	}
}
