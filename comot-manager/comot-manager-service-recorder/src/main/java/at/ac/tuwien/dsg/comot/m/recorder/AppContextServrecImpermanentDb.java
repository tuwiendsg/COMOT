package at.ac.tuwien.dsg.comot.m.recorder;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(AppContextServrec.IMPERMANENT_NEO4J_DB)
public class AppContextServrecImpermanentDb {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new TestGraphDatabaseFactory().newImpermanentDatabase();
	}

}
