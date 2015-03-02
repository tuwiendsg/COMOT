package at.ac.tuwien.dsg.comot.m.core.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;

@Configuration
@PropertySource({ "classpath:spring/properties/application.properties" })
@Profile(AppContextCore.INSERT_INIT_DATA)
public class AppContextCoreInsertData {

	public static final Logger log = LoggerFactory.getLogger(AppContextCoreInsertData.class);

	@javax.annotation.Resource
	public Environment env;

	@Autowired
	public SalsaClient salsaClient;
	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected InformationServiceMock infoServ;

	@Bean
	public Object insertData() {

		return null;
	}
}
