package at.ac.tuwien.dsg.comot.m.adapter;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.eps.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.m.cs.ControlClientRsybl;
import at.ac.tuwien.dsg.comot.m.cs.DeploymentClientSalsa;
import at.ac.tuwien.dsg.comot.m.cs.MonitoringClientMela;
import at.ac.tuwien.dsg.comot.m.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;

@Configuration
@EnableTransactionManagement
@PropertySource({ "classpath:properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.adapter" })
@Import({ AppContextEps.class })
@EnableAsync
public class AppContextAdapter {

	public static final Logger log = LoggerFactory.getLogger(AppContextAdapter.class);

	public static final String SERVER = "localhost";

	@Autowired
	protected ApplicationContext context;
	@Resource
	public Environment env;

	public AppContextAdapter() {
		super();
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(SERVER);
		return connectionFactory;
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		RabbitAdmin admin = new RabbitAdmin(connectionFactory());
		return admin;
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		return template;
	}

	// clients

	@Bean
	@Scope("prototype")
	public DeploymentClient deploymentClient() throws URISyntaxException {
		return new DeploymentClientSalsa(
				new SalsaClient(new URI(env.getProperty("uri.deployemnt"))));
	}

	@Bean
	@Scope("prototype")
	public MonitoringClient monitoringClient() throws URISyntaxException {
		return new MonitoringClientMela(
				new MelaClient(new URI(env.getProperty("uri.monitoring"))));
	}

	@Bean
	@Scope("prototype")
	public ControlClient controlClient() throws URISyntaxException {
		return new ControlClientRsybl(
				new RsyblClient(new URI(env.getProperty("uri.controller"))));
	}

}
