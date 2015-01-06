package at.ac.tuwien.dsg.comot.recorder;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile(AppContextRecorder.SPRING_PROFILE_TEST)
public class AppContextRecorderTest  {

	private static final String DB_PATH = "target/data/db";

	
	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new ImpermanentGraphDatabase(DB_PATH);

	}

}
