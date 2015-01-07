package at.ac.tuwien.dsg.comot.recorder.test;

import java.util.Iterator;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestBean {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;
	@Autowired
	protected ExecutionEngine engine;

	@Transactional
	public void test() {

		Iterator<Relationship> iter = engine.execute("match (n)-[r:connectTo]->(m) return r").columnAs("r");
		for (Relationship rel : IteratorUtil.asIterable(iter)) {
			for (String prop : rel.getPropertyKeys()) {
				log.info("{}: {}={}", rel, prop, rel.getProperty(prop));
			}
		}
	}

}
