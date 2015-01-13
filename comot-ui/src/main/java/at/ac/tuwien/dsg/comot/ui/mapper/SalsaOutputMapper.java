package at.ac.tuwien.dsg.comot.ui.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.model.logic.RelationshipResolver;
import at.ac.tuwien.dsg.comot.model.node.NodeInstance;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.ui.model.ElementState;

@Component
public class SalsaOutputMapper {

	protected final Logger log = LoggerFactory.getLogger(SalsaOutputMapper.class);

	public static final String SERVICE = "SERVICE";
	public static final String TOPOLOGY = "TOPOLOGY";

	public ElementState extractOutput(CloudService service) {

		RelationshipResolver resolver = new RelationshipResolver(service);

		ElementState root = new ElementState(service.getId(), SERVICE, service.getState().toString());
		List<ElementState> topologies = new ArrayList<>();

		doTopologies(root, service.getServiceTopologies(), resolver, topologies);

		List<ElementState> tempList = null;

		// create hierarchical structure based on HOST_ON
		for (ElementState eTopo : topologies) {
			tempList = new ArrayList<>();

			for (ElementState one : eTopo.getChildren()) {

				// temp list to point all elements
				tempList.add(one);

				// set CONNECT_TO
				// one.setConnectToId(resolver.getConnectToIds(one.getId()));
			}

			for (Iterator<ElementState> iterator = eTopo.getChildren().iterator(); iterator.hasNext();) {
				ElementState one = iterator.next();
				ElementState host = findHost(one, tempList, resolver);

				log.debug("element={}, host={}", one.getId(), ((host == null) ? null : host.getId()));

				if (host != null) {
					iterator.remove();
					host.addChild(one);
				}
			}
		}

		return root;
	}

	protected void doTopologies(ElementState parent, Set<ServiceTopology> topologies, RelationshipResolver resolver,
			List<ElementState> eTopos) {

		boolean isUnit;

		for (ServiceTopology topology : topologies) {

			ElementState element = new ElementState(topology.getId(), TOPOLOGY, topology.getState().toString());
			eTopos.add(element);
			parent.addChild(element);

			doTopologies(element, topology.getServiceTopologies(), resolver, eTopos);

			for (StackNode node : topology.getNodes()) {
				isUnit = resolver.isServiceUnit(node.getId());

				if (node.getInstances().size() == 0) {
					ElementState one = new ElementState(node.getId(), node.getType().toString(), topology
							.getState().toString());
					element.addChild(one);
					element.setServiceUnit(isUnit);

				} else {
					for (NodeInstance instance : node.getInstances()) {
						ElementState one = new ElementState(node.getId(), node.getType().toString(), topology
								.getState().toString());
						one.setInstanceId(instance.getInstanceId());
						element.addChild(one);
						element.setServiceUnit(isUnit);
					}
				}
			}

		}
	}

	protected ElementState findHost(ElementState element, List<ElementState> list, RelationshipResolver resolver) {

		String hostId = resolver.getHostId(element.getId());
		if (hostId == null) {
			return null;
		}

		if (element.getInstanceId() == null) {// stack node
			for (ElementState temp : list) {
				if (temp.getId().equals(hostId)) {
					return temp;
				}
			}

		} else {// instance
			NodeInstance host = resolver.navigator().getInstance(element.getId(), element.getInstanceId())
					.getHostInstance();
			if (host == null) {
				return null;
			}

			for (ElementState temp : list) {
				if (temp.getId().equals(hostId) && temp.getInstanceId() == host.getInstanceId()) {
					return temp;
				}
			}
		}

		return null;
	}
}
