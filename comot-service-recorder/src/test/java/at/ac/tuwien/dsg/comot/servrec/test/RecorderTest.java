package at.ac.tuwien.dsg.comot.servrec.test;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.model.repo.CloudServiceRepo;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.test.model.examples.ServiceTemplates;

public class RecorderTest extends AbstractTest {

	protected CloudService service;

	@Autowired
	protected CloudServiceRepo cloudServiceRepo;

	@Before
	public void startup() {
		service = ServiceTemplates.fullService();
	}

	@Test
	public void aaaaa() {
		cloudServiceRepo.save(service);
	}

	@Test
	public void testTemp() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			ClassNotFoundException, ComotException, RecorderException {

		revisionApi.createOrUpdateRegion(service, service.getId(), "init");

		CloudService service2 = (CloudService) revisionApi.getRevision(service.getId(), "serviceId",
				System.currentTimeMillis());

		assertReflectionEquals(service, service2, ReflectionComparatorMode.LENIENT_ORDER);

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
			if (node.getId().equals(ServiceTemplates.swNodeId2)) {
				node.getConnectToList().get(0).setVariableValue("variableValue_UPDATED");
			}
		}

		revisionApi.createOrUpdateRegion(service, service.getId(), "mychange");

		testBean.test();

		while (true) {
			Thread.sleep(1000);
		}

	}

	public void sleepSeconds(int seconds) {
		try {
			log.debug("Waiting {} seconds", seconds);
			Thread.sleep(seconds * 1000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
