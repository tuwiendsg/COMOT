/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.recorder.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class AutomatedTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(AutomatedTest.class);

	protected CloudService service;

	@Test
	public void testSimpleServiceOneVersion() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, RecorderException {

		service = STemplates.simplifiedService();
		oneVersion();

		// UtilsTest.sleepInfinit();
	}

	@Test
	public void testFullServiceOneVersion() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, RecorderException {

		service = STemplates.fullService();
		oneVersion();
	}

	public void oneVersion() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, ComotException, RecorderException {

		cutOsus(service);

		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, STemplates.serviceId, "init", null);

		CloudService sResult = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, System.currentTimeMillis());

		assertReflectionEquals(service, sResult, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void testSimpleServiceMultipleVersions() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, IOException,
			RecorderException, JAXBException {

		service = STemplates.simplifiedService();
		multipleVersions();

	}

	@Test
	public void testFullServiceMultipleVersions() throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, ComotException, InterruptedException, IOException,
			RecorderException, JAXBException {

		service = STemplates.fullService();
		multipleVersions();

		// UtilsTest.sleepInfinit();
	}

	public void multipleVersions() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException,
			IOException, InstantiationException, ComotException, RecorderException, JAXBException {

		cutOsus(service);

		ServiceUnit unitV1 = UtilsTest.getServiceUnit(service, STemplates.swNodeId);

		// VERSION 1
		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, STemplates.serviceId, "init", null);

		Long version1Time = System.currentTimeMillis();

		// VERSION 2
		CloudService updatedService = update1(service);
		ServiceUnit unitV2 = UtilsTest.getServiceUnit(updatedService, STemplates.swNodeId);
		revisionApi.createOrUpdateRegion(updatedService, STemplates.serviceId, STemplates.serviceId, "config_change",
				null);

		UtilsTest.sleepInfinit();

		Long version2Time = System.currentTimeMillis();

		// VERSION 3
		CloudService finalService = update2(updatedService);
		ServiceUnit unitV3 = UtilsTest.getServiceUnit(finalService, STemplates.swNodeId);
		revisionApi.createOrUpdateRegion(finalService, STemplates.serviceId, STemplates.serviceId, "config_change",
				null);

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
		cutOsus(service);

		Long change1Time;
		Long change3Time;
		Change change;

		// VERSION 1
		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, STemplates.serviceId, "init", null);

		// VERSION 2
		CloudService updatedService = update1(service);
		revisionApi.createOrUpdateRegion(updatedService, STemplates.serviceId, STemplates.serviceId, "config_change",
				null);

		// VERSION 3
		CloudService finalService = update2(updatedService);
		revisionApi.createOrUpdateRegion(finalService, STemplates.serviceId, STemplates.serviceId, "config_change",
				null);

		// READ CHANGES - WHOLE TIME
		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId, 0L,
				Long.MAX_VALUE);
		assertEquals(3, countChanges(change));

		change1Time = change.getTimestamp();
		change3Time = change.getTo().getEnd().getTo().getEnd().getTimestamp();

		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.swNodeId, 0L,
				Long.MAX_VALUE);
		assertEquals(2, countChanges(change));
		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.swNodeId2, 0L,
				Long.MAX_VALUE);
		assertEquals(3, countChanges(change));// because of connect to relationship

		// READ CHANGES - SELECTED PERIOD

		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId,
				change1Time, Long.MAX_VALUE);
		assertEquals(3, countChanges(change));
		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId,
				change1Time + 1, Long.MAX_VALUE);
		assertEquals(2, countChanges(change));
		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId, 0L,
				change3Time);
		assertEquals(3, countChanges(change));
		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId, 0L,
				change3Time - 1);
		assertEquals(2, countChanges(change));
		change = revisionApi
				.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId, change1Time + 1,
						change3Time - 1);
		assertEquals(1, countChanges(change));

		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId, 0L,
				change1Time - 1);
		assertEquals(0, countChanges(change));

		// READ REVISION AT SPECIFIC TIME

		CloudService sResult2 = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, change1Time + 1);
		assertReflectionEquals(service, sResult2, ReflectionComparatorMode.LENIENT_ORDER);

		// GET ALL IDs

		List<ManagedObject> list = revisionApi.getManagedObjects(STemplates.serviceId);
		assertEquals(6, list.size());
		LOG.info("{}", list);

	}

	@Test
	public void testRevisionsAndEventsAll() throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, IOException, InstantiationException, RecorderException, JAXBException {

		service = STemplates.simplifiedService();
		cutOsus(service);

		Change change = null;
		String typeEvent = "event";
		String prop1 = "PROP_ORIGIN";
		String prop2 = "PROP_TARGET";
		String prop3 = "PROP_NON_STRING";

		// VERSION 1
		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, STemplates.serviceId, "init", null);

		// EVENT
		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put(prop1, "SALSA");
		changeProperties.put(prop2, "tomcat");
		changeProperties.put(prop3, System.currentTimeMillis());
		revisionApi.storeEvent(STemplates.serviceId, STemplates.serviceId, typeEvent, changeProperties);

		// VERSION 2
		CloudService updatedService = update1(service);
		revisionApi.createOrUpdateRegion(updatedService, STemplates.serviceId, STemplates.serviceId, "config_change",
				null);

		// VERSION 3
		CloudService finalService = update2(updatedService);
		revisionApi.createOrUpdateRegion(finalService, STemplates.serviceId, STemplates.serviceId, "config_change",
				null);

		change = revisionApi.getAllChangesThatModifiedThisObject(STemplates.serviceId, STemplates.serviceId,
				0L, Long.MAX_VALUE);
		assertEquals(3, countChanges(change));

		change = revisionApi.getAllChanges(STemplates.serviceId, 0L, Long.MAX_VALUE);
		assertEquals(4, countChanges(change));

		boolean found = false;

		while (change != null) {

			if (change.getType().equals(typeEvent)) {
				found = true;

				LOG.info(Utils.asXmlString(change));
				LOG.info(Utils.asJsonString(change));

				assertEquals(changeProperties.get(prop1), change.getPropertiesMap().get(prop1));
				assertEquals(changeProperties.get(prop2), change.getPropertiesMap().get(prop2));
				assertEquals(changeProperties.get(prop3), change.getPropertiesMap().get(prop3));
			}
			change = change.getTo().getEnd();
		}

		if (!found) {
			fail("event change not found");
		}
	}

	@Test
	public void testNewRevisionOnlyForSubObject() throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, IOException, InstantiationException, RecorderException {

		service = STemplates.fullService();
		cutOsus(service);

		String targetInstanceId = STemplates.instanceId(STemplates.swNodeId);

		// VERSION 1
		revisionApi.createOrUpdateRegion(service, STemplates.serviceId, STemplates.serviceId, "init", null);

		CloudService updatedService = (CloudService) Utils.deepCopy(service);
		Navigator nav = new Navigator(updatedService);
		UnitInstance uInst = nav.getInstance(targetInstanceId);
		uInst.setEnvId("Some new value");

		revisionApi.createOrUpdateRegion(uInst, STemplates.serviceId, targetInstanceId, "change", null);

		CloudService resultService = (CloudService) revisionApi.getRevision(STemplates.serviceId,
				STemplates.serviceId, Long.MAX_VALUE);
		assertReflectionEquals(updatedService, resultService, ReflectionComparatorMode.LENIENT_ORDER);

	}

}
