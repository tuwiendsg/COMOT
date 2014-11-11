package at.ac.tuwien.dsg.comot.cs.mapper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.DeploymentOrika;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

@Component
public class DeploymentMapper {

	protected final Logger log = LoggerFactory.getLogger(DeploymentMapper.class);

	@Autowired
	protected DeploymentOrika mapper;

	public DeploymentDescription extractDeployment(CloudService cloudService) throws ClassNotFoundException,
			IOException {

		DeploymentDescription descr = mapper.get().map(cloudService, DeploymentDescription.class);

		log.trace("Final mapping: {}", Utils.asXmlStringLog(descr));
		return descr;
	}

	public void enrichModel(CloudService cloudService, DeploymentDescription deployment) {

		mapper.get().map(deployment, cloudService);
	}
}
