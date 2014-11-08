package at.ac.tuwien.dsg.comot.common.test.example;

import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.type.ServiceUnitPropertiesType;
import at.ac.tuwien.dsg.comot.common.model.type.ServiceUnitType;
import at.ac.tuwien.dsg.comot.common.model.unit.Properties;

public class ExampleDeployOneVM {

	public static final String SERVICE_ID = "example_deployOneVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	
	public static CloudService build(){
		
		Properties properties = new Properties(ServiceUnitPropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "openjdk-7-jre");
		
		ServiceUnit unit = new ServiceUnit(NODE_ID, ServiceUnitType.OS);
		unit.setProperties(properties);

		ServiceTopology topology = new ServiceTopology(TOPOLOGY_ID);
		topology.addUnit(unit);
		
		CloudService service = new CloudService(SERVICE_ID);
		service.addServiceTopology(topology);
		
		return service;
		
	}
}
