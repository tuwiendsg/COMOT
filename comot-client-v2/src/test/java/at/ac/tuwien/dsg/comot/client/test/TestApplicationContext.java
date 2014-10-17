package at.ac.tuwien.dsg.comot.client.test;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.client.DeploymentClientSalsa;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;

@Configuration
// @PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot" })
public class TestApplicationContext {

	@Resource
	public Environment env;
	
	@Bean
	public DeploymentClient deploymentClient(){
		DeploymentClient client = new DeploymentClientSalsa();
		client.setHost("128.130.172.215");
		client.setPort(8080);
		
		return client;
	}

}
