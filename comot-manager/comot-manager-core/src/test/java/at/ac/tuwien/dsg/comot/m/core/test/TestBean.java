package at.ac.tuwien.dsg.comot.m.core.test;

import java.util.Iterator;
import java.util.List;

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
	public Double computeDuration(String region) {

		Iterator<Double> iter = engine.execute("match (r:_REGION {_id: '"+region+"'} )-[:_FIRST_REV]-> ()-[relCol *]->(m:_Revision) UNWIND relCol as rel return avg(rel.timestamp - rel.`properties-eventTime`) as m").columnAs("m");
		for (Double ll : IteratorUtil.asIterable(iter)) {
			return ll;
		}
		return null;
	}


}
