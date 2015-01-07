package at.ac.tuwien.dsg.comot.graph;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;

@Configuration
@EnableNeo4jRepositories
@ComponentScan
public class AppContextGraph extends Neo4jConfiguration {

	public static final String SPRING_PROFILE_PROD = "prod";
	public static final String SPRING_PROFILE_TEST = "test";

	public AppContextGraph() {
		setBasePackage("at.ac.tuwien.dsg.comot");
	}

}
