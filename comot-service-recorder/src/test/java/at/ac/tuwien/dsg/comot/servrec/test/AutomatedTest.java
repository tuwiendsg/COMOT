package at.ac.tuwien.dsg.comot.servrec.test;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.recorder.model.Change;
import at.ac.tuwien.dsg.comot.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class AutomatedTest extends AbstractTest {

	protected CloudService service;

	@Test
	public void testSimpleServiceOneVersion() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, RecorderException {

		service = STemplates.simplifiedService();
		oneVersion();

	}

	@Test
	public void testFullServiceOneVersion() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, RecorderException {

		service = STemplates.fullService();
		oneVersion();
	}

	public void oneVersion() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, ComotException, RecorderException {

		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, "init");

		CloudService sResult = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, System.currentTimeMillis());

		assertReflectionEquals(service, sResult, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void testSimpleServiceMultipleVersions() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, IOException,
			RecorderException {

		service = STemplates.simplifiedService();
		multipleVersions();

		// UtilsTest.sleepInfinit();
	}

	@Test
	public void testFullServiceMultipleVersions() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, IOException,
			RecorderException {

		service = STemplates.fullService();
		multipleVersions();

		// UtilsTest.sleepInfinit();
	}

	public void multipleVersions() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException,
			IOException, InstantiationException, ComotException, RecorderException {

		ServiceUnit unitV1 = UtilsTest.getServiceUnit(service, STemplates.swNodeId);

		// VERSION 1
		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, "init");

		Long version1Time = System.currentTimeMillis();

		// VERSION 2
		CloudService updatedService = update1(service);
		ServiceUnit unitV2 = UtilsTest.getServiceUnit(updatedService, STemplates.swNodeId);
		revisionApi.createOrUpdateRegion(updatedService, STemplates.serviceId, "config_change");

		Long version2Time = System.currentTimeMillis();

		// VERSION 3
		CloudService finalService = update2(updatedService);
		ServiceUnit unitV3 = UtilsTest.getServiceUnit(finalService, STemplates.swNodeId);
		revisionApi.createOrUpdateRegion(finalService, STemplates.serviceId, "config_change");

		// ///////////////////////////
		// READ VERSION 1
		CloudService sResult = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, version1Time);
		assertReflectionEquals(service, sResult, ReflectionComparatorMode.LENIENT_ORDER);

		// READ VERSION 2
		CloudService sResult2 = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, version2Time);
		assertReflectionEquals(updatedService, sResult2, ReflectionComparatorMode.LENIENT_ORDER);

		// READ VERSION 3
		CloudService sResult3 = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, Long.MAX_VALUE);
		assertReflectionEquals(finalService, sResult3, ReflectionComparatorMode.LENIENT_ORDER);

		// READ ServiceUnit

		ServiceUnit unitV1Res = (ServiceUnit) revisionApi.getRevision(STemplates.serviceId,
				STemplates.swNodeId, version1Time);
		assertReflectionEquals(unitV1, unitV1Res, ReflectionComparatorMode.LENIENT_ORDER);

		// READ VERSION 2
		ServiceUnit unitV2Res = (ServiceUnit) revisionApi.getRevision(STemplates.serviceId,
				STemplates.swNodeId, version2Time);
		assertReflectionEquals(unitV2, unitV2Res, ReflectionComparatorMode.LENIENT_ORDER);

		// READ VERSION 3
		ServiceUnit unitV3Res = (ServiceUnit) revisionApi.getRevision(STemplates.serviceId,
				STemplates.swNodeId, Long.MAX_VALUE);
		assertReflectionEquals(unitV3, unitV3Res, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void testRevisionFiltering() throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, IOException, InstantiationException, RecorderException {

		service = STemplates.simplifiedService();
		Long change1Time;
		Long change3Time;

		// VERSION 1
		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, "init");

		// VERSION 2
		CloudService updatedService = update1(service);
		revisionApi.createOrUpdateRegion(updatedService, STemplates.serviceId, "config_change");

		// VERSION 3
		CloudService finalService = update2(updatedService);
		revisionApi.createOrUpdateRegion(finalService, STemplates.serviceId, "config_change");

		// UtilsTest.sleepInfinit();

		// READ CHANGES - WHOLE TIME
		Change change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.serviceId, 0L, Long.MAX_VALUE);
		assertEquals(3, countChanges(change));

		change1Time = change.getTimestamp();
		change3Time = change.getTo().getEnd().getTo().getEnd().getTimestamp();

		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.swNodeId, 0L, Long.MAX_VALUE);
		assertEquals(2, countChanges(change));
		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.swNodeId2, 0L, Long.MAX_VALUE);
		assertEquals(3, countChanges(change));// because of connect to relationship

		// READ CHANGES - SELECTED PERIOD

		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.serviceId, change1Time, Long.MAX_VALUE);
		assertEquals(3, countChanges(change));
		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.serviceId, change1Time + 1, Long.MAX_VALUE);
		assertEquals(2, countChanges(change));
		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.serviceId, 0L, change3Time);
		assertEquals(3, countChanges(change));
		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.serviceId, 0L, change3Time - 1);
		assertEquals(2, countChanges(change));
		change = revisionApi
				.getAllChanges(STemplates.serviceId, STemplates.serviceId, change1Time + 1, change3Time - 1);
		assertEquals(1, countChanges(change));

		change = revisionApi.getAllChanges(STemplates.serviceId, STemplates.serviceId, 0L, change1Time - 1);
		assertEquals(0, countChanges(change));

		// READ REVISION AT SPECIFIC TIME

		CloudService sResult2 = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, change1Time + 1);
		assertReflectionEquals(service, sResult2, ReflectionComparatorMode.LENIENT_ORDER);

		// GET ALL IDs

		List<ManagedObject> list = revisionApi.getManagedObjects(STemplates.serviceId);
		assertEquals(7, list.size());
		log.info("{}", list);

	}

}
