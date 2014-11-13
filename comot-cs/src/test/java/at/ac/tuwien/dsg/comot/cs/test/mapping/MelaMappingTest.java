package at.ac.tuwien.dsg.comot.cs.test.mapping;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.cs.mapper.MelaMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.UtilsMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.ObjectFactory;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class MelaMappingTest extends AbstractTest {

	@Autowired
	protected MelaOrika orika;
	@Autowired
	protected MelaMapper mapper;

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException {

		 //log.info("original {}", Utils.asJsonString(serviceForMapping));
		
		 MonitoredElement element = mapper.extractMela(serviceForMapping);
		 log.info("mela {}", UtilsMapper.asString(element));
		
	}
	
	@Test
	public void requirementsTest() throws JAXBException, ClassNotFoundException, IOException {

		 //log.info("original {}", Utils.asJsonString(serviceForMapping));
		
		 Requirements element = mapper.extractRequirements(serviceForMapping);
		 log.info("mela {}", Utils.asXmlString(element));
		
	}

	@Test
	public void orikaTest() throws JAXBException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		MonitoredElement element = orika.get().map(serviceForMapping, MonitoredElement.class);
		log.info("element {}", UtilsMapper.asString(element));
		

	}

}
