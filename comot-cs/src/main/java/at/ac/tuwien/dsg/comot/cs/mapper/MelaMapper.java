package at.ac.tuwien.dsg.comot.cs.mapper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Relationship;

@Component
public class MelaMapper {

	protected final Logger log = LoggerFactory.getLogger(MelaMapper.class);

	@Autowired
	protected MelaOrika mapper;

	public MonitoredElement extractMela(CloudService cloudService) {

		MonitoredElement element;
		Relationship tempRel;

		// Navigator navigator = new Navigator(cloudService);
		MonitoredElement root = mapper.get().map(cloudService, MonitoredElement.class);

		Map<String, MonitoredElement> map = extractAllElements(root);
		
		for(MonitoredElement me: map.values()){
			log.info(me.getId());
		}

		for (EntityRelationship rel : cloudService.getRelationships()) {
			log.info("original {}", rel.getToPart());
			
			if (map.containsKey(rel.getFromPart().getId()) 
					&& map.containsKey(rel.getToPart().getId()) 
					&& !rel.getType().equals(RelationshipType.LOCAL)) {
				element = map.get(rel.getFromPart().getId());
				tempRel = new Relationship()
						.withFrom(map.get(rel.getFromPart().getId()))
						.withTo(map.get(rel.getToPart().getId()))
						.withType(resolveType(rel.getType()));

				element.getRelationships().add(tempRel);
			}
		}

		log.trace("Final mapping: {}", Utils.asXmlStringLog(root));

		return root;
	}

	protected Map<String, MonitoredElement> extractAllElements(MonitoredElement element) {

		Map<String, MonitoredElement> map = new HashMap<>();
		map.put(element.getId(), element);

		for (MonitoredElement child : element.getContainedElements()) {
			map.putAll(extractAllElements(child));
		}
		return map;
	}

	// TODO InConjunctionWith seems not to be eqivalent with anything in tosca
	protected Relationship.RelationshipType resolveType(RelationshipType type) {
		if (type.equals(RelationshipType.CONNECT_TO)) {
			return Relationship.RelationshipType.ConnectedTo;

		} else if (type.equals(RelationshipType.HOST_ON)) {
			return Relationship.RelationshipType.HostedOn;

		} else if (type.equals(RelationshipType.LOCAL)) {
			throw new UnsupportedOperationException();

		} else {
			throw new UnsupportedOperationException();
		}
	}

}
