package at.ac.tuwien.dsg.comot.cs.test.mapping;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.UtilsMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.ToscaOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;

public class ToscaMappingTest extends AbstractTest {

	@Autowired
	protected ToscaMapper myMapper;

	@Test
	public void mapperTest() throws JAXBException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions tosca1 = myMapper.toTosca(serviceForMapping);
		log.info("tosca1 {}", UtilsMapper.asString(tosca1));

		CloudService service2 = myMapper.toModel(tosca1);
		log.info("service2 {}", Utils.asJsonString(service2));

		Definitions tosca2 = myMapper.toTosca(service2);
		log.info("tosca2 {}", UtilsMapper.asString(tosca2));

		CloudService service3 = myMapper.toModel(tosca1);
		log.info("service3 {}", Utils.asJsonString(service3));

	}

	@Test
	public void orikaTest() throws JAXBException {

		ToscaOrika toscaOrika = new ToscaOrika();

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions tosca1 = toscaOrika.get().map(serviceForMapping, Definitions.class);
		log.info("tosca1 {}", UtilsMapper.asString(tosca1));

		CloudService service2 = toscaOrika.get().map(tosca1, CloudService.class);
		log.info("service2 {}", Utils.asJsonString(service2));

		Definitions tosca2 = toscaOrika.get().map(service2, Definitions.class);
		log.info("tosca2 {}", UtilsMapper.asString(tosca2));

		CloudService service3 = toscaOrika.get().map(tosca2, CloudService.class);
		log.info("service3 {}", Utils.asJsonString(service3));

	}

}
