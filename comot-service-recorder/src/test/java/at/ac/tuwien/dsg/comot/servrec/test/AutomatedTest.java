package at.ac.tuwien.dsg.comot.servrec.test;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;

import org.junit.Test;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.test.model.examples.ServiceTemplates;

public class AutomatedTest extends AbstractTest {

	protected CloudService service;

	@Test
	public void testSimpleServiceOneVersion() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, RecorderException {

		service = ServiceTemplates.simplifiedService();
		oneVersion();

	}

	@Test
	public void testSimpleServiceMultipleVersions() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, IOException,
			RecorderException {

		service = ServiceTemplates.simplifiedService();
		multipleVersions();
	}

	@Test
	public void testFullServiceOneVersion() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, RecorderException {

		service = ServiceTemplates.fullService();
		oneVersion();
	}

	@Test
	public void testFullServiceMultipleVersions() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, IOException,
			RecorderException {

		service = ServiceTemplates.fullService();
		multipleVersions();
	}

	public void oneVersion() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, ComotException, RecorderException {

		revisionApi.createOrUpdateRegion(service, ServiceTemplates.serviceId, "init");

		CloudService sResult = (CloudService) revisionApi.getRevision(ServiceTemplates.serviceId,
				ServiceTemplates.serviceId, System.currentTimeMillis());

		assertReflectionEquals(service, sResult, ReflectionComparatorMode.LENIENT_ORDER);

	}

	public void multipleVersions() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException,
			IOException, InstantiationException, ComotException, RecorderException {

		// ///////////////////////////
		// VERSION 1
		revisionApi.createOrUpdateRegion(service, ServiceTemplates.serviceId, "init");

		Long version1Time = System.currentTimeMillis();

		// ///////////////////////////
		// VERSION 2
		CloudService updatedService = (CloudService) Utils.deepCopy(service);// createService();

		// add parameter -> create new state & update relation to the old one
		updatedService.setName("UPDATED");

		ServiceTopology topo = updatedService.getServiceTopologiesList().get(0);
		ServiceUnit unit = topo.getServiceUnitsList().get(0);

		// remove relationship -> update timestamp
		topo.getServiceUnits().remove(unit);

		// add node
		ServiceTopology newTopo = new ServiceTopology("newTopo_UPDATED");

		// add relationship s
		newTopo.addServiceUnit(unit);
		updatedService.addServiceTopology(newTopo);

		// change parameter of relationship -> create new one & update state of the old
		for (StackNode node : topo.getNodes()) {
			if (node.getId().equals(ServiceTemplates.swNodeId2) && node.getConnectToList().size() > 0) {
				node.getConnectToList().get(0).setVariableValue("variableValue_UPDATED");
			}
		}

		revisionApi.createOrUpdateRegion(updatedService, ServiceTemplates.serviceId, "config_change");

		Long version2Time = System.currentTimeMillis();

		// ///////////////////////////
		// VERSION 3
		CloudService finalService = (CloudService) Utils.deepCopy(updatedService);

		// add parameter -> create new state & update relation to the old one
		finalService.setName("UPDATE_2");

		ServiceTopology topo1 = finalService.getServiceTopologiesList().get(0);
		ServiceTopology topo2 = finalService.getServiceTopologiesList().get(1);
		ServiceUnit unit2 = topo1.getServiceUnitsList().get(0);

		// remove relationship -> update timestamp
		topo1.getServiceUnits().remove(unit2);
		finalService.getServiceTopologies().remove(topo1);

		// add relationship s
		topo2.addTopology(topo1);

		revisionApi.createOrUpdateRegion(finalService, ServiceTemplates.serviceId, "config_change");

		// ///////////////////////////
		// READ VERSION 1
		CloudService sResult = (CloudService) revisionApi.getRevision(ServiceTemplates.serviceId,
				ServiceTemplates.serviceId, version1Time);
		assertReflectionEquals(service, sResult, ReflectionComparatorMode.LENIENT_ORDER);

		// READ VERSION 2
		CloudService sResult2 = (CloudService) revisionApi.getRevision(ServiceTemplates.serviceId,
				ServiceTemplates.serviceId, version2Time);
		assertReflectionEquals(updatedService, sResult2, ReflectionComparatorMode.LENIENT_ORDER);

		// READ VERSION 3
		CloudService sResult3 = (CloudService) revisionApi.getRevision(ServiceTemplates.serviceId,
				ServiceTemplates.serviceId, Long.MAX_VALUE);
		assertReflectionEquals(finalService, sResult3, ReflectionComparatorMode.LENIENT_ORDER);

	}

	public void assertLabels(Long expected, Class<?> clazz) {
		assertLabels(expected, clazz.getSimpleName());
	}

	public void assertLabels(Long expected, String label) {
		Long count = testBean.countLabel(label);
		assertEquals(expected, count);
	}

}
