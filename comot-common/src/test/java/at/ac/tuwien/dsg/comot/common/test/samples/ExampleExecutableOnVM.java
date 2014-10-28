package at.ac.tuwien.dsg.comot.common.test.samples;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;

public class ExampleExecutableOnVM {

	public static final String SERVICE_ID = "example_ExecutableOnVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String OS_ID = "example_OS_comot";
	//public static final String ARTIFACT_ID = "example_SW";

	public static CloudService build() {

		OperatingSystemUnit os = OperatingSystemUnit(OS_ID)
				.providedBy(OpenstackMicro("example_VM")
						.addSoftwarePackage("openjdk-7-jre"))
				.andMaxInstances(5);

		ServiceUnit artifact = SingleSoftwareUnit("helloWorld")
				.deployedBy(SingleScriptArtifactTemplate("deployScript",
						"http://128.130.172.215/salsa/upload/files/comot/run.sh"))
				.andMaxInstances(5);

		ServiceTopology topology = ServiceTopology(TOPOLOGY_ID)
				.withServiceUnits(os, artifact);

		CloudService serviceTemplate = ServiceTemplate(SERVICE_ID)
				.consistsOfTopologies(topology)
				.andRelationships(
						HostedOnRelation("app_on_os")
							.from(artifact)
							.to(os))
				.withDefaultMetrics()
				.withDefaultActionEffects();

		return serviceTemplate;
	}
}
