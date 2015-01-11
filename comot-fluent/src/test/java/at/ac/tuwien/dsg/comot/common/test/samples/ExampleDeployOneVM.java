package at.ac.tuwien.dsg.comot.common.test.samples;

import static at.ac.tuwien.dsg.comot.common.fluent.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.fluent.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.fluent.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.fluent.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.fluent.CloudService;
import at.ac.tuwien.dsg.comot.common.fluent.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.fluent.ServiceTopology;

public class ExampleDeployOneVM {

	public static final String SERVICE_ID = "example_deployOneVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	public static CloudService build() {

		OperatingSystemUnit dataControllerVM = OperatingSystemUnit(NODE_ID)
				.providedBy(OpenstackMicro("example_VM")
						.addSoftwarePackage("openjdk-7-jre"))
				.andMaxInstances(5);

		ServiceTopology topology = ServiceTopology(TOPOLOGY_ID)
				.withServiceUnits(dataControllerVM);

		CloudService serviceTemplate = ServiceTemplate(SERVICE_ID)
				.consistsOfTopologies(topology)
				.withDefaultMetrics()
				.withDefaultActionEffects();

		return serviceTemplate;
	}
}