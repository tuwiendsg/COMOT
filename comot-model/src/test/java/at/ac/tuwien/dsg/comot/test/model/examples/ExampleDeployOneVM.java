package at.ac.tuwien.dsg.comot.test.model.examples;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class ExampleDeployOneVM {

	public static final String SERVICE_ID = "example_deployOneVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	public static CloudService build() {

		
//		OfferedServiceUnit osu = new OfferedServiceUnit("vm", OsuType.OS);
//		osu.addResource("instanceType", "000000512");
//		osu.addResource("provider", "dsg@openstack");
//		osu.addResource("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
//		osu.addResource("packages", "openjdk-7-jre");
//
//		ServiceUnit unit = new ServiceUnit(NODE_ID, "example_name", 1, 1);
//		unit.setOsu(osu);
//		
//		ServiceTopology topology = new ServiceTopology(TOPOLOGY_ID);
//		topology.addServiceUnit(unit);
//
//		CloudService service = new CloudService(SERVICE_ID);
//		service.addServiceTopology(topology);
//
//		return service;
            return null;

	}
}
