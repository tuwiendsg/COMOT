package at.ac.tuwien.dsg.comot.servrec.spring;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

@Configuration
@Profile(AppContextServrec.STANDALONE_NEO4J_DB)
public class AppContextServrecEmbeddedDb {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private static final String DB_PATH = "target/data/db";

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		// GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase("target/data/db");
		log.info("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
		GraphDatabaseService db = new SpringRestGraphDatabase("http://localhost:7474/db/data/");
		log.info("Neo4jDB: {}", db);
		return db;
	}
}
