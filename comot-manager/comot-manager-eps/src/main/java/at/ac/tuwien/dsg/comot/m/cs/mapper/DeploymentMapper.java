package at.ac.tuwien.dsg.comot.m.cs.mapper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.DeploymentOrika;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

@Component
public class DeploymentMapper {

	protected final Logger log = LoggerFactory.getLogger(DeploymentMapper.class);

	@Autowired
	protected DeploymentOrika mapperDepl;

	public DeploymentDescription extractDeployment(CloudService cloudService) {

		DeploymentDescription descr = mapperDepl.get().map(cloudService, DeploymentDescription.class);

		log.trace("Final mapping: {}", Utils.asXmlStringLog(descr));
		return descr;
	}

	public Map<String, String> extractStates(String csInstanceId,
			at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState) {

		Map<String, String> map = new HashMap<>();

		for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology topology : serviceState
				.getComponentTopologyList()) {
			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit unit : topology.getComponents()) {
				for (ServiceInstance instance : unit.getInstancesList()) {
					map.put(
							IdResolver.uniqueInstance(csInstanceId, unit.getId(), instance.getInstanceId()),
							instance.getState().toString());
				}
			}
		}
		return map;
	}

	public void enrichModel(CloudService cloudService,
			at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState) {

		Navigator navigator = new Navigator(cloudService);
		Map<String, String> hosts = new HashMap<>();

		String csInstanceId = cloudService.getInstancesList().get(0).getId();

		for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology topology : serviceState
				.getComponentTopologyList()) {
			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit unit : topology.getComponents()) {

				ServiceUnit node = navigator.getUnitFor(unit.getId());
				UnitInstance nInst;
				UnitInstance existingInst;

				for (ServiceInstance instance : unit.getInstancesList()) {
					existingInst = null;
					for (UnitInstance cInst : node.getInstances()) {
						if (IdResolver.uniqueInstance(csInstanceId, unit.getId(), instance.getInstanceId())
						== cInst.getId()) {
							existingInst = cInst;
							break;
						}
					}

					if (existingInst == null) {
						nInst = new UnitInstance(
								IdResolver.uniqueInstance(csInstanceId, unit.getId(), instance.getInstanceId()),
								null, // TODO here set IP
								convert(instance.getState()),
								null);
						node.addUnitInstance(nInst);

					} else {
						existingInst.setState(convert(instance.getState()));
					}

					// <instanceID, hostInstanceID> pairs
					if (instance.getHostedId_Integer() != Integer.MAX_VALUE) {
						hosts.put(
								IdResolver.uniqueInstance(csInstanceId, unit.getId(), instance.getInstanceId()),
								IdResolver.uniqueInstance(csInstanceId, unit.getHostedId(),
										instance.getHostedId_Integer()));
					}
				}
			}
		}
		navigator = new Navigator(cloudService);

		// set host to instance
		for (String str : hosts.keySet()) {
			UnitInstance node = navigator.getInstance(str);
			UnitInstance host = navigator.getInstance(hosts.get(str));
			node.setHostInstance(host);
		}
	}

	public static State convert(SalsaEntityState state) {
		switch (state) {
		case UNDEPLOYED:
			return State.STARTING;
		case ALLOCATING:
			return State.DEPLOYING;
		case STAGING:
			return State.DEPLOYING;
		case CONFIGURING:
			return State.DEPLOYING;
		case INSTALLING:
			return State.DEPLOYING;
		case DEPLOYED:
			return State.RUNNING;
		case ERROR:
			return State.ERROR;
		case STAGING_ACTION:
			return State.STAGING_ACTION;
		default:
			return null;
		}
	}

	public static State convert(String state) {
		return convert(SalsaEntityState.valueOf(state));
	}

}
