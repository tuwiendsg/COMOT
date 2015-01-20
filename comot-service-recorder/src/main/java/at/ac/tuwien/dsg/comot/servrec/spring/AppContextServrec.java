package at.ac.tuwien.dsg.comot.servrec.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.AppContextModel;
import at.ac.tuwien.dsg.comot.recorder.AppContextRecorder;

@EnableAsync
@Configuration
@EnableTransactionManagement
@Import({ AppContextModel.class, AppContextRecorder.class, AppContextCore.class })
@ComponentScan("at.ac.tuwien.dsg.comot.servrec")
@Transactional
public class AppContextServrec {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String IMPERMANENT_NEO4J_DB = "IMPERMANENT_NEO4J_DB";
	public static final String STANDALONE_NEO4J_DB = "STANDALONE_NEO4J_DB";
	public static final String EMBEDDED_NEO4J_DB = "EMBEDDED_NEO4J_DB";

}
