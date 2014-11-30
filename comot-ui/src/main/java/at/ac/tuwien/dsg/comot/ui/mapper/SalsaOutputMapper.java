package at.ac.tuwien.dsg.comot.ui.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.common.model.logic.RelationshipResolver;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.ui.model.ElementState;

@Component
public class SalsaOutputMapper {

	protected final Logger log = LoggerFactory.getLogger(SalsaOutputMapper.class);

	@Autowired
	protected SalsaOutputOrika mapper;

	public ElementState extractOutput(CloudService cloudService) {

		Navigator navigator = new Navigator(cloudService);
		RelationshipResolver resolver = new RelationshipResolver(cloudService);

		ElementState root = mapper.get().map(cloudService, ElementState.class);

		List<ElementState> tempList = null;

		// create hierarchical structure based on HOST_ON
		for (ElementState eTopo : getAllTopologies(root)) {
			tempList = new ArrayList<>();

			for (ElementState one : eTopo.getChildren()) {
				// set ServiceUnits
				if (resolver.isServiceUnit(one.getId())) {
					one.setServiceUnit(true);
				}
				// temp list to point all elements
				tempList.add(one);

				// set CONNECT_TO
				one.setConnectToId(resolver.getConnectToIds(one.getId()));
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

	protected List<ElementState> getAllTopologies(ElementState element) {

		List<ElementState> topologies = new ArrayList<>();

		if (element.getType().equals(ElementState.Type.TOPOLOGY)) {
			topologies.add(element);
		}

		for (ElementState child : element.getChildren()) {
			topologies.addAll(getAllTopologies(child));
		}
		return topologies;
	}

	protected ElementState findHost(ElementState element, List<ElementState> list, RelationshipResolver resolver) {

		String hostId = resolver.getHostId(element.getId());

		if (element.getInstanceId() == null) {// no instances
			for (ElementState temp : list) {
				if (temp.getId().equals(hostId)) {
					return temp;
				}
			}

		} else {// with instances
			int hostInstanceId = resolver.navigator().getInstance(element.getId(), element.getInstanceId())
					.getHostedId();

			for (ElementState temp : list) {
				if (temp.getId().equals(hostId) && temp.getInstanceId() == hostInstanceId) {
					return temp;
				}
			}
		}

		return null;
	}
}
