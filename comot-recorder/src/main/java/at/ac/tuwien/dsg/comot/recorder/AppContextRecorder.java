package at.ac.tuwien.dsg.comot.recorder;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jRepositories
@ComponentScan
@EnableTransactionManagement
public class AppContextRecorder extends Neo4jConfiguration {

	public static final String SPRING_PROFILE_PROD = "prod";
	public static final String SPRING_PROFILE_TEST = "test";

	@Autowired
	protected GraphDatabaseService db;

	public AppContextRecorder() {
		setBasePackage("at.ac.tuwien.dsg.comot");
	}

	@Bean
	public ExecutionEngine executionEngine() {
		return new ExecutionEngine(db);
	}

}
