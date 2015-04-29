package at.ac.tuwien.dsg.comot.m.recorder;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile(AppContextServrec.EMBEDDED_NEO4J_DB)
public class AppContextServrecEmbeddedDb {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected Environment env;

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(env
				.getProperty("recorder.neo4j.embedded"));
		log.info("Neo4jDB: {}", db);
		return db;
	}

}
