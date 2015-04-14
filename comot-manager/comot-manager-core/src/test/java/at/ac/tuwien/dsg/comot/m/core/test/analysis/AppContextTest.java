package at.ac.tuwien.dsg.comot.m.core.test.analysis;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// @PropertySource({ "classpath:properties/application.properties" })
// @Import({ AppContextCore.class })
public class AppContextTest {

	public static final String DB_PATH = "src/test/resources/data/db";

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		return db;
	}

}
