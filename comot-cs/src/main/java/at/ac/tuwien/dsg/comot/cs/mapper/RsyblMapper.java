package at.ac.tuwien.dsg.comot.cs.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;

@Component
public class RsyblMapper {

	protected final Logger log = LoggerFactory.getLogger(RsyblMapper.class);

	@Autowired
	protected RsyblOrika mapper;

	public CloudServiceXML extractRsybl(CloudService cloudService) {

		// cloudService = (CloudService) UtilsMapper.deepCopy(cloudService);
		// Navigator navigator = new Navigator(cloudService);

		// TODO check with Georgiana if SW units should really be removed
		// ignore SOFTWARE nodes
		// for (StackNode unit : navigator.getAllNodes()) {
		// if (unit.getType().equals(SwType.SOFTWARE)) {
		// navigator.getParentTopology(unit.getId()).getServiceUnits().remove(unit);
		// }
		// }

		CloudServiceXML serviceXml = mapper.get().map(cloudService, CloudServiceXML.class);

		// log.trace("Final mapping: {}", );

		return serviceXml;
	}
}
