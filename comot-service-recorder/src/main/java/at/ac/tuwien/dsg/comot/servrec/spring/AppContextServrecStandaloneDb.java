package at.ac.tuwien.dsg.comot.servrec.spring;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(AppContextServrec.STANDALONE_NEO4J_DB)
public class AppContextServrecStandaloneDb {

	private static final String DB_PATH = "target/data/db";

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new GraphDatabaseFactory().newEmbeddedDatabase("target/data/db");
		// new GraphDatabaseFactory().newEmbeddedDatabase("target/data/db")
		// new SpringRestGraphDatabase("http://localhost:7474/db/data/");
	}

}
