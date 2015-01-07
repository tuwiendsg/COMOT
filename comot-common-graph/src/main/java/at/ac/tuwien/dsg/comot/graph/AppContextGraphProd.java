package at.ac.tuwien.dsg.comot.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

@Configuration
@Profile(AppContextGraph.SPRING_PROFILE_PROD)
public class AppContextGraphProd {

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new SpringRestGraphDatabase("http://localhost:7474/db/data/");

	}

}
