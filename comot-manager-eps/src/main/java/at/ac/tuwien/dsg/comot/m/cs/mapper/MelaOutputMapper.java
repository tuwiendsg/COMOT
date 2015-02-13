package at.ac.tuwien.dsg.comot.m.cs.mapper;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.MelaOutputOrika;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;

@Component
public class MelaOutputMapper {

	protected final Logger log = LoggerFactory.getLogger(MelaOutputMapper.class);

	@Autowired
	protected MelaOutputOrika mapper;

	public ElementMonitoring extractOutput(MonitoredElementMonitoringSnapshot snapshot) throws JAXBException {

		ElementMonitoring root = mapper.get().map(snapshot, ElementMonitoring.class);

		// log.debug("Final mapping: {}", Utils.asJsonString(root));

		return root;
	}

}
