package at.ac.tuwien.dsg.comot.cs.test.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.UtilsMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.DeploymentOrika;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.ObjectFactory;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;

public class DeploymentMappingTest extends AbstractTest {

	@Autowired
	protected DeploymentOrika orika;
	@Autowired
	protected DeploymentMapper mapper;

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException {

		AssociatedVM vm = new AssociatedVM();
		vm.setIp("10.99.0.85");
		vm.setUuid("93d785cc-f915-4127-81eb-0797b75de1a6");

		List<AssociatedVM> list = new ArrayList<>();
		list.add(vm);

		DeploymentUnit unit = new DeploymentUnit();
		unit.setServiceUnitID(swNodeId);
		unit.setAssociatedVM(list);

		List<DeploymentUnit> deployments = new ArrayList<>();
		deployments.add(unit);

		DeploymentDescription descr = new DeploymentDescription();
		descr.setAccessIP("localhost");
		descr.setCloudServiceID(serviceId);
		descr.setDeployments(deployments);

		log.info("original depl {}", UtilsMapper.asString(descr));
		log.info("original service {}", Utils.asJsonString(serviceForMapping));

		mapper.enrichModel(serviceForMapping, descr);
		log.info("enriched service {}", Utils.asJsonString(serviceForMapping));
		
		DeploymentDescription descr2 = mapper.extractDeployment(serviceForMapping);
		log.info("depl2 {}", UtilsMapper.asString(descr2));

	}

}
