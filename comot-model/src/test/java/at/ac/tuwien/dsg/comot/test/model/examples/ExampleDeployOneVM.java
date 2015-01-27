package at.ac.tuwien.dsg.comot.test.model.examples;

import at.ac.tuwien.dsg.comot.model.node.Properties;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.model.type.NodeType;

public class ExampleDeployOneVM {

	public static final String SERVICE_ID = "example_deployOneVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	public static CloudService build() {

		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "openjdk-7-jre");

		ServiceUnit unit = new ServiceUnit(NODE_ID, "example_name", 1, 1, NodeType.OS);
		unit.addProperties(properties);

		ServiceTopology topology = new ServiceTopology(TOPOLOGY_ID);
		topology.addServiceUnit(unit);

		CloudService service = new CloudService(SERVICE_ID);
		service.addServiceTopology(topology);

		return service;

	}
}
