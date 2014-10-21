package at.ac.tuwien.dsg.comot.client.test;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.client.ControlClientRsybl;
import at.ac.tuwien.dsg.comot.client.DeploymentClientSalsa;
import at.ac.tuwien.dsg.comot.client.MonitoringClientMela;
import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;

@Configuration
// @PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.client" })
public class TestClientContext {

	@Resource
	public Environment env;

	@Bean
	public DeploymentClient deploymentClient() {
		DeploymentClient client = new DeploymentClientSalsa();
		client.setHost("128.130.172.215");
		client.setPort(8080);

		return client;
	}

	@Bean
	public MonitoringClient monitoringClient() {
		MonitoringClient client = new MonitoringClientMela();
		client.setHost("localhost");
		client.setPort(8180);

		return client;
	}

	@Bean
	public ControlClient controlClient() {
		ControlClient client = new ControlClientRsybl();
		client.setHost("localhost");
		client.setPort(8280);

		return client;
	}

}