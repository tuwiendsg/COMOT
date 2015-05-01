/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.cs.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceInstance;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.ElasticityCapability;

@Component
public class DeploymentMapper {

	private static final Logger LOG = LoggerFactory.getLogger(DeploymentMapper.class);

	public DeploymentDescription extractDeployment(CloudService service) {

		DeploymentDescription description = new DeploymentDescription();
		description.setCloudServiceID(service.getId());

		Navigator navigator = new Navigator(service);
		DeploymentUnit depl;
		List<AssociatedVM> vms;

		for (ServiceUnit unit : navigator.getAllUnits()) {
			if (navigator.isTrueServiceUnit(unit.getId())) {

				ServiceUnit os = navigator.getOsForServiceUnit(unit.getId());

				depl = new DeploymentUnit();
				depl.setServiceUnitID(unit.getId());
				vms = new ArrayList<>();

				for (UnitInstance instance : os.getInstances()) {
					AssociatedVM vm = new AssociatedVM();
					vm.setIp(instance.getIp());
					vm.setUuid(instance.getEnvId());
					vms.add(vm);
				}
				depl.setAssociatedVMs(vms);

				// TODO this should be taken from model, not hard coded
				if (unit.getDirectives() != null && !unit.getDirectives().isEmpty()) {

					ElasticityCapability scaleOut = new ElasticityCapability();
					scaleOut.setName("scaleOut");
					scaleOut.setPrimitiveOperations("scaleOut");

					ElasticityCapability scaleIn = new ElasticityCapability();
					scaleIn.setName("scaleIn");
					scaleIn.setPrimitiveOperations("scaleIn");

					depl.addElasticityCapability(scaleOut);
					depl.addElasticityCapability(scaleIn);
				}

				description.getDeployments().add(depl);
			}
		}

		LOG.trace("Final mapping: {}", Utils.asXmlStringLog(description));
		return description;
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

	public void enrichModel(String csInstanceId, CloudService cloudService,
			at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState) {

		Navigator navigator = new Navigator(cloudService);
		Map<String, String> hosts = new HashMap<>();

		for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceTopology topology : serviceState
				.getComponentTopologyList()) {
			for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit unit : topology.getComponents()) {

				ServiceUnit node = navigator.getUnitFor(unit.getId());
				UnitInstance comotInstance;

				for (ServiceInstance salsaInst : unit.getInstancesList()) {
					comotInstance = null;
					for (UnitInstance cInst : node.getInstances()) {
						if (IdResolver.uniqueInstance(csInstanceId, unit.getId(), salsaInst.getInstanceId())
						== cInst.getId()) {
							comotInstance = cInst;
							break;
						}
					}

					if (comotInstance == null) {
						comotInstance = new UnitInstance(
								IdResolver.uniqueInstance(csInstanceId, unit.getId(), salsaInst.getInstanceId()),
								null,
								convert(salsaInst.getState()),
								null);

						addVMmInfoIfThereIsSome(salsaInst, comotInstance);
						node.addUnitInstance(comotInstance);

					} else {
						comotInstance.setState(convert(salsaInst.getState()));
					}

					// <instanceID, hostInstanceID> pairs
					if (salsaInst.getHostedId_Integer() != Integer.MAX_VALUE) {
						hosts.put(
								IdResolver.uniqueInstance(csInstanceId, unit.getId(), salsaInst.getInstanceId()),
								IdResolver.uniqueInstance(csInstanceId, unit.getHostedId(),
										salsaInst.getHostedId_Integer()));
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

	public void addVMmInfoIfThereIsSome(ServiceInstance salsaInst, UnitInstance instane) {

		if (salsaInst.getProperties() != null && salsaInst.getProperties().getAny() != null) {

			SalsaInstanceDescription_VM desc = (SalsaInstanceDescription_VM) salsaInst.getProperties().getAny();
			String ip = desc.getPrivateIp();
			String uuid = desc.getInstanceId();

			instane.setIp(ip);
			instane.setEnvId(uuid);

		}

	}

	public static State convert(SalsaEntityState state) {
		switch (state) {
		case UNDEPLOYED:
			return State.UNDEPLOYING;
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
			return null;
		default:
			return null;
		}
	}

	public static String runningToState() {
		return SalsaEntityState.DEPLOYED.toString();
	}

	public static State convert(String state) {
		return convert(SalsaEntityState.valueOf(state));
	}

}
