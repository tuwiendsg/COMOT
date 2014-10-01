package at.ac.tuwien.dsg.ElasticityInformationService.Application;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import at.ac.tuwien.dsg.ElasticityInformationService.settings.EliseConfiguration;

@Configuration
@EnableNeo4jRepositories(basePackages = "at.ac.tuwien.dsg.ElasticityInformationService")
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
public class RunService extends Neo4jConfiguration {
	public RunService() {
		setBasePackage("at.ac.tuwien.dsg.ElasticityInformationService");
	}

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
	}

	public static void main(String[] args) {
		SpringApplication.run(RunService.class, args);
	}
}
