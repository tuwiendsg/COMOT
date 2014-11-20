package at.ac.tuwien.dsg.elise.Application;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableNeo4jRepositories
@EnableWebMvc
@ComponentScan
@Import(RepositoryRestMvcConfiguration.class)
public class RunService extends Neo4jConfiguration {
	public RunService() {
		setBasePackage("at.ac.tuwien.dsg.elise");		
	}

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		// Change this to switch between embedded or remote database server
		
		//return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
		return new SpringRestGraphDatabase("http://localhost:7474/db/data/");
	}
	
	public static void main(String[] args) {
		SpringApplication.run(RunService.class, args);
	}
}
