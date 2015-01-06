package at.ac.tuwien.dsg.comot.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile(AppContextGraph.SPRING_PROFILE_TEST)
public class AppContextGraphTest  {

	private static final String DB_PATH = "target/data/db";

	
	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new ImpermanentGraphDatabase(DB_PATH);

	}

}
