package at.ac.tuwien.dsg.comot.cs.mapper;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.Navigator;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;

@Component
public class RsyblMapper {

	protected final Logger log = LoggerFactory.getLogger(RsyblMapper.class);

	protected RsyblOrika mapper;

	public RsyblMapper() {
		mapper = new RsyblOrika();
	}

	public CloudServiceXML toRsybl(CloudService cloudService) throws JAXBException {

		CloudServiceXML serviceXml = mapper.get().map(cloudService, CloudServiceXML.class);
		Navigator navigator = new Navigator(cloudService);

		log.trace("Mapping by dozer: {}");

		log.trace("Final mapping: {}");

		return serviceXml;
	}

}
