package at.ac.tuwien.dsg.comot.model;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableNeo4jRepositories
@ComponentScan
@Transactional
public class AppContextModel extends Neo4jConfiguration {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public AppContextModel() {
		setBasePackage("at.ac.tuwien.dsg.comot");
	}

	@Autowired
	protected GraphDatabaseService db;

	@Bean
	public ExecutionEngine executionEngine() {
		return new ExecutionEngine(db);
	}

}
