package at.ac.tuwien.dsg.comot.recorder.test;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.graph.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.graph.test.ConvertingToGraph;

public class RecorderTest extends AbstractTest {

	protected static final Logger log = LoggerFactory.getLogger(RecorderTest.class);

	protected CloudService service;
	protected String swNodeId = "nodeId";
	protected String swNodeId2 = "nodeId2";
	protected String serviceId = "serviceId";
	ServiceTopology topology;

	@Before
	public void startup() {
		service = ConvertingToGraph.createService();
	}

	@Test
	public void testTemplate() throws InterruptedException, IllegalArgumentException, IllegalAccessException {

		revisionApi.createOrUpdateRegion(service, service.getId(), "init");

		sleepSeconds(0);

		log.info("EXECUTE UPDATE");

		// UPDATE
		service.setName("UPDATED");

		ServiceTopology topo = service.getServiceTopologiesList().get(0);
		ServiceUnit unit = topo.getServiceUnitsList().get(0);

		topo.getServiceUnits().remove(unit);
		ServiceTopology newTopo = new ServiceTopology("newTopo_UPDATED");
		newTopo.addServiceUnit(unit);

		service.addServiceTopology(newTopo);

		for (StackNode node : topo.getNodes()) {
			if (node.getId().equals(ConvertingToGraph.swNodeId2)) {
				node.getConnectToList().get(0).setVariableValue("variableValue_UPDATED");
			}
		}

		revisionApi.createOrUpdateRegion(service, service.getId(), "mychange");

		testBean.test();

		while (true) {
			Thread.sleep(1000);
		}

	}

	public static void sleepSeconds(int seconds) {
		try {
			log.debug("Waiting {} seconds", seconds);
			Thread.sleep(seconds * 1000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
