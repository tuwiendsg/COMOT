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

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextServrec.class, AppContextEps.class })
@ActiveProfiles({ AppContextServrec.IMPERMANENT_NEO4J_DB })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

	@Autowired
	protected ApplicationContext context;
	@Resource
	protected Environment env;

	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected TestBean testBean;

	@Autowired
	protected ToscaMapper mapperTosca;

	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected ExecutionEngine engine;
	@Autowired
	protected ChangeRepo changeRepo;

	protected WrappingNeoServerBootstrapper srv;

	@Before
	public void setUp() {
		// http://neo4j.com/docs/1.8.3/server-embedded.html
		// http://127.0.0.1:7474/
		srv = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) db);
		srv.start();
	}

	@After
	public void cleanUp() {
		srv.stop();
	}

	public static CloudService update1(CloudService service) throws ClassNotFoundException, IOException {
		CloudService updatedService = (CloudService) Utils.deepCopy(service);// createService();

		// add parameter -> create new state & update relation to the old one
		updatedService.setName("UPDATED");

		ServiceTopology topo = updatedService.getServiceTopologiesList().get(0);
		ServiceUnit unit = UtilsTest.getServiceUnit(updatedService, STemplates.swNodeId);

		// remove relationship -> update timestamp
		topo.getServiceUnits().remove(unit);

		unit.setName("serviceNameUPDATED");

		// add node
		ServiceTopology newTopo = new ServiceTopology("newTopo_UPDATED");

		// add relationship s
		newTopo.addServiceUnit(unit);
		updatedService.addServiceTopology(newTopo);

		// change parameter of relationship -> create new one & update state of the old
		for (ServiceUnit node : topo.getServiceUnits()) {
			if (node.getId().equals(STemplates.swNodeId2) && node.getConnectToList().size() > 0) {
				node.getConnectToList().get(0).setVariableValue("variableValue_UPDATED");
			}
		}
		return updatedService;
	}

	public static CloudService update2(CloudService service) throws ClassNotFoundException, IOException {

		CloudService finalService = (CloudService) Utils.deepCopy(service);

		// add parameter -> create new state & update relation to the old one
		finalService.setName("UPDATE_2");

		ServiceTopology topo1 = finalService.getServiceTopologiesList().get(0);
		ServiceTopology topo2 = finalService.getServiceTopologiesList().get(1);
		ServiceUnit unit2 = UtilsTest.getServiceUnit(finalService, STemplates.swNodeId2);

		// remove relationship -> update timestamp
		topo1.getServiceUnits().remove(unit2);
		finalService.getServiceTopologies().remove(topo1);

		// add relationship s
		topo2.addTopology(topo1);

		return finalService;
	}

	public void assertLabels(Long expected, Class<?> clazz) {
		assertLabels(expected, clazz.getSimpleName());
	}

	public void assertLabels(Long expected, String label) {
		Long count = testBean.countLabel(label);
		assertEquals(expected, count);
	}

	public int countChanges(Change change) {
		int i = 0;
		while (change != null) {
			i++;
			LOG.debug("stateFROM: {}", change.getFrom());
			LOG.debug("change: {}", change);
			LOG.debug("stateTO: {}\n", change.getTo());
			change = change.getTo().getEnd();
		}
		return i;
	}

	public static void cutOsus(CloudService service) {
		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setOsuInstance(null);
		}

	}

}
