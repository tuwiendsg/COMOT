package at.ac.tuwien.dsg.comot.graph.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.graph.model.ConnectToRelationship;
import at.ac.tuwien.dsg.comot.graph.model.ContractItem;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.XSelectable;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactReference;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.graph.model.node.NodeInstance;
import at.ac.tuwien.dsg.comot.graph.model.node.NodeInstanceOs;
import at.ac.tuwien.dsg.comot.graph.model.node.Properties;
import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.graph.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.graph.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.graph.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.graph.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.graph.model.type.NodeType;
import at.ac.tuwien.dsg.comot.graph.model.type.State;

public class ConvertingToGraph extends AbstractTest {

	protected CloudService service;
	public static final String swNodeId = "nodeId";
	public static final String swNodeId2 = "nodeId2";
	public static final String serviceId = "serviceId";

	@Before
	public void startup() {
		service = ServiceTemplates.fullService();
	}

	

	// start and check at http://127.0.0.1:7474/
	@Test
	public void testStuff() throws InterruptedException {
		
		CloudService serviceFromDb = serviceRepo.save(service);

		ContractItem item = new ContractItem();
		item.addProperty(serviceFromDb);
		item.addProperty((XSelectable) serviceFromDb.getServiceTopologies().toArray()[0]);

		contractItemRepo.save(item);

		while (true) {
			Thread.sleep(1000);
		}
	}

}
