package at.ac.tuwien.dsg.comot.m.ui.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.ui.model.ElementState;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@Component
public class SalsaOutputMapper {

	protected final Logger log = LoggerFactory.getLogger(SalsaOutputMapper.class);

	public static final String SERVICE = "SERVICE";
	public static final String TOPOLOGY = "TOPOLOGY";

	public ElementState extractOutput(CloudService service) {

		Navigator navigator = new Navigator(service);

		ElementState root = new ElementState(service.getId(), SERVICE, service.getState().toString());
		List<ElementState> topologies = new ArrayList<>();

		doTopologies(root, service.getServiceTopologies(), navigator, topologies);

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
				ElementState host = findHost(one, tempList, navigator);

				log.debug("element={}, host={}", one.getId(), ((host == null) ? null : host.getId()));

				if (host != null) {
					iterator.remove();
					host.addChild(one);
				}
			}
		}

		return root;
	}

	protected void doTopologies(ElementState parent, Set<ServiceTopology> topologies, Navigator navigator,
			List<ElementState> eTopos) {

		ElementState eTopo, eNode, eInstance;
		boolean isUnit;

		for (ServiceTopology topology : topologies) {
			log.info("topology.getState() {}", topology.getState());
			eTopo = new ElementState(topology.getId(), TOPOLOGY, topology.getState().toString());
			eTopos.add(eTopo);
			parent.addChild(eTopo);

			doTopologies(eTopo, topology.getServiceTopologies(), navigator, eTopos);

			for (ServiceUnit node : topology.getServiceUnits()) {
				isUnit = navigator.isTrueServiceUnit(node.getId());

				if (node.getInstances().size() == 0) {
					eNode = new ElementState(node.getId(), node.getOsu().getType().toString(), node
							.getState().toString());
					eNode.setServiceUnit(isUnit);

					eTopo.addChild(eNode);

				} else {
					for (UnitInstance instance : node.getInstances()) {
						eInstance = new ElementState(node.getId(), node.getOsu().getType().toString(), instance
								.getState().toString());
						eInstance.setInstanceId(instance.getInstanceId());
						eInstance.setServiceUnit(isUnit);

						eTopo.addChild(eInstance);
					}
				}
			}

		}
	}

	protected ElementState findHost(ElementState element, List<ElementState> list, Navigator navigator) {

		ServiceUnit hostUnit = navigator.getHost(element.getId());
		if (hostUnit == null) {
			return null;
		}

		if (element.getInstanceId() == null) {// stack node
			for (ElementState temp : list) {
				if (temp.getId().equals(hostUnit.getId())) {
					return temp;
				}
			}

		} else {// instance
			UnitInstance host = navigator.getInstance(element.getId(), element.getInstanceId())
					.getHostInstance();
			if (host == null) {
				return null;
			}

			for (ElementState temp : list) {
				if (temp.getId().equals(hostUnit.getId()) && temp.getInstanceId() == host.getInstanceId()) {
					return temp;
				}
			}
		}

		return null;
	}
}
