package at.ac.tuwien.dsg.comot.test.model.examples;

import java.util.UUID;

import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.devel.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.OsuType;
import at.ac.tuwien.dsg.comot.model.type.State;

public class STemplates {

	public static final String swNodeId = "nodeId";
	public static final String swNodeId2 = "nodeId2";
	public static final String osNodeId = "osId";
	public static final String topologyId = "topologyId";
	public static final String serviceId = "serviceId";

	public static CloudService simplifiedService() {

		// OSUs
		OfferedServiceUnit osuOs = new OfferedServiceUnit();
		osuOs.setName(osNodeId);
		osuOs.setTypeByEnum(OsuType.OS);
		osuOs.hasResource(new Resource("000000512", new ResourceOrQualityType("instanceType")));
		osuOs.hasResource(new Resource("dsg@openstack", new ResourceOrQualityType("provider")));
		osuOs.hasResource(new Resource("8f1428ac-f239-42e0-ab35-137f6e234101", new ResourceOrQualityType("baseImage")));
		osuOs.hasResource(new Resource("openjdk-7-jre", new ResourceOrQualityType("packages")));

		OfferedServiceUnit osuSw = new OfferedServiceUnit();
		osuSw.setName(swNodeId);
		osuSw.setTypeByEnum(OsuType.SOFTWARE);

		OfferedServiceUnit osuSw2 = new OfferedServiceUnit();
		osuSw2.setName(swNodeId2);
		osuSw2.setTypeByEnum(OsuType.SOFTWARE);

		// UNITs
		ServiceUnit unitOs = new ServiceUnit(osNodeId, "Test os", 1, 2);
		unitOs.setOsuInstance(new OsuInstance(osNodeId + "_1", osuOs));

		ServiceUnit unitSw = new ServiceUnit(swNodeId, "Test node unit", 2, 5);
		unitSw.setOsuInstance(new OsuInstance(swNodeId + "_1", osuSw));

		ServiceUnit unitSw2 = new ServiceUnit(swNodeId2, "Test node unit 2", 2, 5);
		unitSw2.setOsuInstance(new OsuInstance(swNodeId2 + "_1", osuSw2));

		// HOST ON
		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", unitSw, unitOs);
		unitSw.setHost(hostOn1);
		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", unitSw2, unitOs);
		unitSw2.setHost(hostOn2);

		// CONNECT TO
		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", unitSw2, unitSw);
		unitSw2.addConnectTo(rel);

		// TOPOLOGY
		ServiceTopology topology = new ServiceTopology(topologyId);
		topology.addServiceUnit(unitSw);
		topology.addServiceUnit(unitSw2);
		topology.addServiceUnit(unitOs);

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);
		return service;
	}

	public static CloudService fullServiceWithoutInstances() {

		// OSUs
		OfferedServiceUnit osuOs = new OfferedServiceUnit();
		osuOs.setId(osuId(osNodeId));
		osuOs.setName(osNodeId);
		osuOs.setTypeByEnum(OsuType.OS);
		osuOs.hasResource(new Resource("000000512", new ResourceOrQualityType("instanceType")));
		osuOs.hasResource(new Resource("dsg@openstack", new ResourceOrQualityType("provider")));
		osuOs.hasResource(new Resource("8f1428ac-f239-42e0-ab35-137f6e234101", new ResourceOrQualityType("baseImage")));
		osuOs.hasResource(new Resource("openjdk-7-jre", new ResourceOrQualityType("packages")));

		Resource url = new Resource(
				"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh",
				new ResourceOrQualityType(ResourceOrQualityType.ART_REFERENCE_TYPE));
		Resource war = new Resource("deployWar", new ResourceOrQualityType("war"));
		war.hasResource(url);

		OfferedServiceUnit osuSw = new OfferedServiceUnit();
		osuSw.setId(osuId(swNodeId));
		osuSw.setName(swNodeId);
		osuSw.setTypeByEnum(OsuType.SOFTWARE);
		osuSw.hasResource(war);

		OfferedServiceUnit osuSw2 = new OfferedServiceUnit();
		osuSw2.setId(osuId(swNodeId2));
		osuSw2.setName(swNodeId2);
		osuSw2.setTypeByEnum(OsuType.SOFTWARE);

		// UNITs
		ServiceUnit unitOs = new ServiceUnit(osNodeId, "Test os", 1, 2);
		unitOs.setOsuInstance(new OsuInstance(osuInstanceId(osNodeId), osuOs));
		unitOs.setElasticUnit(false);
		
		ServiceUnit unitSw = new ServiceUnit(swNodeId, "Test node unit", 2, 5);
		unitSw.setOsuInstance(new OsuInstance(osuInstanceId(swNodeId), osuSw));
		unitSw.setElasticUnit(true);
		
		unitSw.addDirective(new SyblDirective("ST1", DirectiveType.STRATEGY,
				"ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
		unitSw.addDirective(new SyblDirective("Co2", DirectiveType.CONSTRAINT,
				"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));

		ServiceUnit unitSw2 = new ServiceUnit(swNodeId2, "Test node unit 2", 2, 5);
		unitSw2.setOsuInstance(new OsuInstance(osuInstanceId(swNodeId2), osuSw2));
		unitSw2.setElasticUnit(true);
		
		// HOST ON
		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", unitSw, unitOs);
		unitSw.setHost(hostOn1);
		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", unitSw2, unitOs);
		unitSw2.setHost(hostOn2);

		// CONNECT TO
		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", unitSw2, unitSw);
		unitSw2.addConnectTo(rel);

		// TOPOLOGY
		ServiceTopology topology = new ServiceTopology("topologyId");
		topology.addServiceUnit(unitSw);
		topology.addServiceUnit(unitOs);
		topology.addServiceUnit(unitSw2);
		topology.addDirective(new SyblDirective("Co4", DirectiveType.CONSTRAINT,
				"Co4: CONSTRAINT total_cost < 800"));

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);
		return service;
	}

	public static CloudService fullService() {

		CloudService service = fullServiceWithoutInstances();
		UnitInstance instanceOs = null;

		// INSTANCES
		for (ServiceUnit unit : service.getServiceTopologiesList().get(0).getServiceUnits()) {
			if (unit.getId().equals(osNodeId)) {
				instanceOs = new UnitInstance(instanceId(osNodeId), "10.99.0.1", State.DEPLOYING, null);
				unit.addUnitInstance(instanceOs);
			}
		}

		for (ServiceUnit unit : service.getServiceTopologiesList().get(0).getServiceUnits()) {
			if (unit.getId().equals(swNodeId)) {
				unit.addUnitInstance(new UnitInstance(instanceId(swNodeId), swNodeId + "_processId",
						State.DEPLOYING, instanceOs));
			} else if (unit.getId().equals(swNodeId2)) {
				unit.addUnitInstance(new UnitInstance(instanceId(swNodeId2), swNodeId2 + "_processId",
						State.DEPLOYING, instanceOs));
			}
		}
		return service;
	}

	public static String osuInstanceId(String unitId) {
		return unitId + "_OSU_INSTANCE";
	}

	public static String osuId(String unitId) {
		return unitId + "_OSU";
	}

	public static String instanceId(String unitId) {
		return unitId + "_instance";
	}
}
