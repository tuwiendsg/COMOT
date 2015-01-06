package at.ac.tuwien.dsg.comot.recorder.test;

import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.graph.test.ConvertingToGraph;


public class RecorderTest extends AbstractTest {

	protected CloudService service;
	protected String swNodeId = "nodeId";
	protected String swNodeId2 = "nodeId2";
	protected String serviceId = "serviceId";
	ServiceTopology topology;

	@Before
	public void startup() {
		service = ConvertingToGraph.createService();
	}

	// start and check at http://127.0.0.1:7474/
	@Test
	public void testStuff() throws InterruptedException {

		CloudService serviceFromDb = serviceRepo.save(service);

		while (true) {
			Thread.sleep(1000);
		}
	}

	@Test
	public void testRevisions() throws InterruptedException {

		CloudService serviceFromDb = serviceRepo.save(service);

		revisionApi.saveThis();

		while (true) {
			Thread.sleep(1000);
		}

	}

	@Test
	public void testTemplate() throws InterruptedException, IllegalArgumentException, IllegalAccessException {

		revisionApi.convertGraph(service);

		while (true) {
			Thread.sleep(1000);
		}

	}

}
