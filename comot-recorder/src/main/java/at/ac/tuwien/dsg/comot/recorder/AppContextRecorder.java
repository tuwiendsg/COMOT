package at.ac.tuwien.dsg.comot.recorder;

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

@Configuration
@EnableNeo4jRepositories
@ComponentScan
public class AppContextRecorder extends Neo4jConfiguration {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected GraphDatabaseService db;

	public AppContextRecorder() {
		setBasePackage("at.ac.tuwien.dsg.comot");
	}

	@Bean
	public ExecutionEngine executionEngine() {
		log.info("GraphDatabaseService:  {}", db);
		return new ExecutionEngine(db);
	}

}
