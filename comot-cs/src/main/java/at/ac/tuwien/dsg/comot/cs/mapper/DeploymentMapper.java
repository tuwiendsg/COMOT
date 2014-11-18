package at.ac.tuwien.dsg.comot.cs.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.DeploymentOrika;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.StateOrika;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

@Component
public class DeploymentMapper {

	protected final Logger log = LoggerFactory.getLogger(DeploymentMapper.class);

	@Autowired
	protected DeploymentOrika mapperDepl;

	@Autowired
	protected StateOrika mapperState;

	public DeploymentDescription extractDeployment(CloudService cloudService) {

		DeploymentDescription descr = mapperDepl.get().map(cloudService, DeploymentDescription.class);

		log.trace("Final mapping: {}", Utils.asXmlStringLog(descr));
		return descr;
	}

	public void enrichModel(CloudService cloudService,
			at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState) {

		Navigator navigator = new Navigator(cloudService);

		mapperState.get().map(serviceState, cloudService);

		for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology topology : serviceState
				.getComponentTopologyList()) {

			mapperState.get().map(topology, navigator.getTopology(topology.getId()));

			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit unit : topology.getComponents()) {

				StackNode node = navigator.getNodeFor(unit.getId());
				mapperState.get().map(unit, node);
			}
		}
	}

}
