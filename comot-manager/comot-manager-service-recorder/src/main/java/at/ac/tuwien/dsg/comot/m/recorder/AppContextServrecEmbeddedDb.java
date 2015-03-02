package at.ac.tuwien.dsg.comot.m.recorder;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(AppContextServrec.EMBEDDED_NEO4J_DB)
public class AppContextServrecEmbeddedDb {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String DB_PATH = "target/data/db";

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		log.info("Neo4jDB: {}", db);
		return db;
	}
}
