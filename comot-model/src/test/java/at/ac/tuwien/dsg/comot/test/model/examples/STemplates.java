package at.ac.tuwien.dsg.comot.test.model.examples;

import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.devel.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.ResourceType;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.model.type.OsuType;
import at.ac.tuwien.dsg.comot.model.type.State;

public class STemplates {

	public static final String swNodeId = "nodeId";
	public static final String swNodeId2 = "nodeId2";
	public static final String osNodeId = "osId";
	public static final String topologyId = "topologyId";
	public static final String serviceId = "serviceId";

	public static CloudService simplifiedService() {

		// UNIT OS
//		OfferedServiceUnit osuOs = new OfferedServiceUnit("vm", OsuType.OS);
//		osuOs.addResource("instanceType", "000000512");
//		osuOs.addResource("provider", "dsg@openstack");
//		osuOs.addResource("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
//		osuOs.addResource("packages", "openjdk-7-jre");
//
//		ServiceUnit unitOs = new ServiceUnit(osNodeId, "Test os", 1, 2);
//		unitOs.setOsu(osuOs);
//
//		// UNIT SW 1
//		ServiceUnit unitSw = new ServiceUnit(swNodeId, "Test node unit", 2, 5);
//		unitSw.setOsu(new OfferedServiceUnit(swNodeId+"_OSU", OsuType.SOFTWARE) );
//		// UNIT SW 2
//		ServiceUnit unitSw2 = new ServiceUnit(swNodeId2, "Test node unit 2", 2, 5);
//		unitSw2.setOsu(new OfferedServiceUnit(swNodeId2+"_OSU", OsuType.SOFTWARE) );
//		
//		// HOST ON
//		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", unitSw, unitOs);
//		unitSw.setHost(hostOn1);
//		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", unitSw2, unitOs);
//		unitSw2.setHost(hostOn2);
//
//		// CONNECT TO
//		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", unitSw2, unitSw);
//		unitSw2.addConnectTo(rel);
//
//		// TOPOLOGY
//		ServiceTopology topology = new ServiceTopology(topologyId);
//		topology.addServiceUnit(unitSw);
//		topology.addServiceUnit(unitSw2);
//		topology.addServiceUnit(unitOs);
//
//		CloudService service = new CloudService(serviceId);
//		service.addServiceTopology(topology);
//		return service;
                return null;
	}


	public static CloudService fullServiceWithoutInstances() {

		// UNIT OS
//		OfferedServiceUnit osuOs = new OfferedServiceUnit("vm", OsuType.OS);
//		osuOs.addResource("instanceType", "000000512");
//		osuOs.addResource("provider", "dsg@openstack");
//		osuOs.addResource("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
//		osuOs.addResource("packages", "openjdk-7-jre");
//
//		ServiceUnit unitOs = new ServiceUnit(osNodeId, "Test os", 1, 2);
//		unitOs.setOsu(osuOs);
//
//		// UNIT SW 1
//		OfferedServiceUnit artTemplate = new OfferedServiceUnit("artifactTemplateID", OsuType.SOFTWARE);
//		artTemplate.addResource(ResourceType.SCRIPT.toString(),"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh");
//
//		ServiceUnit unitSw = new ServiceUnit(swNodeId, "Test node unit", 2, 5);
//		unitSw.setOsu(artTemplate);
//
//		unitSw.addDirective(new SyblDirective("str1", DirectiveType.STRATEGY,
//				"ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
//		unitSw.addDirective(new SyblDirective("con1", DirectiveType.CONSTRAINT,
//				"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));
//
//		// UNIT SW 2
//		ServiceUnit unitSw2 = new ServiceUnit(swNodeId2, "Test node unit 2",
//				2, 5);
//		unitSw2.setOsu(new OfferedServiceUnit(swNodeId2+"_OSU", OsuType.SOFTWARE) );
//
//		// HOST ON
//		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", unitSw, unitOs);
//		unitSw.setHost(hostOn1);
//		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", unitSw2, unitOs);
//		unitSw2.setHost(hostOn2);
//
//		// CONNECT TO
//		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", unitSw2, unitSw);
//		unitSw2.addConnectTo(rel);
//
//		// TOPOLOGY
//		ServiceTopology topology = new ServiceTopology("topologyId");
//		topology.addServiceUnit(unitSw);
//		topology.addServiceUnit(unitOs);
//		topology.addServiceUnit(unitSw2);
//		topology.addDirective(new SyblDirective("con4", DirectiveType.CONSTRAINT,
//				"Co4: CONSTRAINT total_cost < 800"));
//
//		CloudService service = new CloudService(serviceId);
//		service.addServiceTopology(topology);
//		return service;
                return null;
	}

	public static CloudService fullService() {

		CloudService service = fullServiceWithoutInstances();
		UnitInstance instanceOs = null;

		// INSTANCES
		for (ServiceUnit unit : service.getServiceTopologiesList().get(0).getServiceUnits()) {
			if (unit.getId().equals(osNodeId)) {
				instanceOs = new UnitInstance(osNodeId + "_instance", 0, "10.99.0.1", State.ALLOCATING, null);
				unit.addUnitInstance(instanceOs);
			}
		}

		for (ServiceUnit unit : service.getServiceTopologiesList().get(0).getServiceUnits()) {
			if (unit.getId().equals(swNodeId)) {
				unit.addUnitInstance(new UnitInstance(swNodeId + "_instance", 0, swNodeId+"_processId",State.DEPLOYED, instanceOs));
			} else if (unit.getId().equals(swNodeId2)) {
				unit.addUnitInstance(new UnitInstance(swNodeId2 + "_instance", 0, swNodeId2+"_processId", State.DEPLOYED, instanceOs));
			}
		}
		return service;

	}
}
