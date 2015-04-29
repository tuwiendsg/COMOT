package at.ac.tuwien.dsg.comot.m.recorder;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import at.ac.tuwien.dsg.comot.model.AppContextModel;

@EnableAsync
@Configuration
@EnableTransactionManagement
@EnableNeo4jRepositories
@Import({ AppContextModel.class })
@ComponentScan
public class AppContextServrec extends Neo4jConfiguration {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String IMPERMANENT_NEO4J_DB = "IMPERMANENT_NEO4J_DB";
	public static final String EMBEDDED_NEO4J_DB = "EMBEDDED_NEO4J_DB";

	@Autowired
	protected GraphDatabaseService db;

	public AppContextServrec() {
		setBasePackage("at.ac.tuwien.dsg.comot.m.recorder");
	}

	@Bean
	public ExecutionEngine executionEngine() {
		log.info("GraphDatabaseService:  {}", db);
		return new ExecutionEngine(db);
	}

}
