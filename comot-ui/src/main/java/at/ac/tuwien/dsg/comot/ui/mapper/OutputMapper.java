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
import at.ac.tuwien.dsg.comot.cs.mapper.orika.RsyblOrika;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.ui.model.Element;

@Component
public class OutputMapper {

	protected final Logger log = LoggerFactory.getLogger(OutputMapper.class);

	@Autowired
	protected OutputOrika mapper;

	public Element extractOutput(CloudService cloudService) {

		Navigator navigator = new Navigator(cloudService);
		RelationshipResolver resolver = new RelationshipResolver(cloudService);

		Element root = mapper.get().map(cloudService, Element.class);

		List<Element> tempList = null;

		// create hierarchical structure based on HOST_ON
		for (Element eTopo : getAllTopologies(root)) {
			tempList = new ArrayList<>();

			for (Element one : eTopo.getChildren()) {
				// set ServiceUnits
				if (resolver.isServiceUnit(one.getId())) {
					one.setServiceUnit(true);
				}
				// temp list to point all elements
				tempList.add(one);
				
				// set CONNECT_TO
				one.setConnectToId(resolver.getConnectToIds(one.getId()));
			}

			for (Iterator<Element> iterator = eTopo.getChildren().iterator(); iterator.hasNext();) {
				Element one = iterator.next();
				Element host = findHost(one, tempList, resolver);

				log.debug("element={}, host={}", one.getId(), ((host == null) ? null : host.getId()));

				if (host != null) {
					iterator.remove();
					host.addChild(one);
				}
			}
		}
		
		

		return root;
	}

	protected List<Element> getAllTopologies(Element element) {

		List<Element> topologies = new ArrayList<>();

		if (element.getType().equals(Element.Type.TOPOLOGY)) {
			topologies.add(element);
		}

		for (Element child : element.getChildren()) {
			topologies.addAll(getAllTopologies(child));
		}
		return topologies;
	}

	protected Element findHost(Element element, List<Element> list, RelationshipResolver resolver) {

		String hostId = resolver.getHostId(element.getId());
		
		if(element.getInstanceId() == null){// no instances
			for (Element temp : list) {
				if (temp.getId().equals(hostId)) {
					return temp;
				}
			}
			
		}else{//with instances
			int hostInstanceId = resolver.navigator().getInstance(element.getId(), element.getInstanceId()).getHostedId();

			for (Element temp : list) {
				if (temp.getId().equals(hostId) && temp.getInstanceId() == hostInstanceId) {
					return temp;
				}
			}
		}
		
		return null;
	}
}
