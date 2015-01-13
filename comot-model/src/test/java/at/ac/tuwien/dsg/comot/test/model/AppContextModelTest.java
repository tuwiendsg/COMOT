package at.ac.tuwien.dsg.comot.test.model;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Transactional
public class AppContextModelTest {

	private static final String DB_PATH = "target/data/db";

	@Autowired
	protected GraphDatabaseService db;

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		// return new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH); // DB will be created in the directory
		// DB_PATH
		return new ImpermanentGraphDatabase(DB_PATH); // directory seems irrelevant, probably just in-memory DB
	}

}
