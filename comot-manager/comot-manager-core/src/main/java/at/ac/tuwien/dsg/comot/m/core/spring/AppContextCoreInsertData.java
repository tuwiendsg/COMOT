package at.ac.tuwien.dsg.comot.m.core.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile(AppContextCore.INSERT_INIT_DATA)
public class AppContextCoreInsertData {

	public static final Logger log = LoggerFactory.getLogger(AppContextCoreInsertData.class);

	@javax.annotation.Resource
	public Environment env;

	@Bean
	public Object insertData() {

		return null;
	}
}
