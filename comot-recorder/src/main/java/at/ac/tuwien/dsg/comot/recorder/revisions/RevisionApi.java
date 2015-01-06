package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.recorder.repo.CloudServiceRepo;
import at.ac.tuwien.dsg.comot.recorder.repo.RevisionRepo;

@Component
public class RevisionApi {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext context;

	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected RevisionRepo revisionRepo;

	@Autowired
	protected CloudServiceRepo serviceRepo;

	@Autowired
	protected GraphDatabaseService db;

	protected Neo4jOperations neo;

	@PostConstruct
	public void setUp() {
		neo = new Neo4jTemplate(db);
	}

	@Transactional
	public void saveThis() {

		CloudService service = serviceRepo.findById("serviceId");

		log.info("aaaa " + service);

		Revision rev1 = new Revision();

		Revision rev2 = new Revision();

		Change change = new Change(Change.ChangeType.CONFIG_UPDATE);
		change.setFrom(rev1);
		change.setTo(rev2);

		rev1.setEnd(change);
		rev2.setStart(change);

		revisionRepo.save(rev1);
		revisionRepo.save(rev2);

		changeRepo.save(change);

		serviceRepo.save(service);

	}

	@Transactional
	public void createService(CloudService service) {

		Map<String, Object> map = new HashMap<>();
		map.put("key", "value");

		neo.createNode(map);

	}

	@Transactional
	public void convertGraph(Object obj) throws IllegalArgumentException, IllegalAccessException {

		context.getBean(SingleConversion.class).convertGraph(obj);
	}

}
