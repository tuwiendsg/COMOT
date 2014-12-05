package at.ac.tuwien.dsg.comot.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;

@Configuration
@EnableNeo4jRepositories
@ComponentScan
public class AppContext extends Neo4jConfiguration {

	private static final String DB_PATH = "target/data/db";

	public AppContext() {
		setBasePackage("at.ac.tuwien.dsg.comot");
	}

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new ImpermanentGraphDatabase(DB_PATH);

	}

}
