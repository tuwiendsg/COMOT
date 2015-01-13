package at.ac.tuwien.dsg.comot.servrec.spring;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import at.ac.tuwien.dsg.comot.model.AppContextModel;
import at.ac.tuwien.dsg.comot.recorder.AppContextRecorder;

@Configuration
@EnableTransactionManagement
@Import({ AppContextModel.class, AppContextRecorder.class })
@ComponentScan("at.ac.tuwien.dsg.comot.servrec")
public class AppContextServrec {

	public static final String SPRING_PROFILE_PROD = "servrec_prod";
	public static final String SPRING_PROFILE_TEST = "servrec_test";

	@Autowired
	protected GraphDatabaseService db;

	@Bean
	public ExecutionEngine executionEngine() {
		return new ExecutionEngine(db);
	}

	private static final String DB_PATH = "target/data/db";

	// @Bean(destroyMethod = "shutdown")
	// public GraphDatabaseService graphDatabaseService() {
	// return new ImpermanentGraphDatabase(DB_PATH);
	//
	// }

}
