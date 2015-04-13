package at.ac.tuwien.dsg.comot.m.core.analytics;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.template.Neo4jOperations;

import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.repo.RevisionRepo;

public class ElasticityAnalyzis {

	private static final Logger log = LoggerFactory.getLogger(ElasticityAnalyzis.class);

	@Autowired
	private ApplicationContext context;
	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;
	@Autowired
	protected ExecutionEngine engine;

	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected RevisionRepo revisionRepo;

}
