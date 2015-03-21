package at.ac.tuwien.dsg.comot.m.core.spring;

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

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.m.cs.ControlClientRsybl;
import at.ac.tuwien.dsg.comot.m.cs.DeploymentClientSalsa;
import at.ac.tuwien.dsg.comot.m.cs.MonitoringClientMela;
import at.ac.tuwien.dsg.comot.m.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;

@Configuration
@EnableTransactionManagement
@PropertySource({ "classpath:properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.core" })
@Import({ AppContextEps.class, AppContextServrec.class })
@EnableAsync
public class AppContextCore {

	public static final Logger log = LoggerFactory.getLogger(AppContextCore.class);

	public static final String INSERT_INIT_DATA = "INSERT_INIT_DATA";

	/**
	 * Key pattern: instanceID.changeTRUE/FALSE.stateBefore.stateAfter.lifeCycleEvent.targetLevel.originId
	 */
	public static final String EXCHANGE_LIFE_CYCLE = "EXCHANGE_LIFE_CYCLE";

	/**
	 * Key pattern: instanceID.epsId.customEvent.targetLevel
	 */
	public static final String EXCHANGE_CUSTOM_EVENT = "EXCHANGE_CUSTOM_EVENT";

	/**
	 * Key pattern: instanceID.EventType.event.targetLevel
	 */
	public static final String EXCHANGE_REQUESTS = "EXCHANGE_REQUESTS";

	/**
	 * Key pattern: instanceID.originId
	 */
	public static final String EXCHANGE_EXCEPTIONS = "EXCHANGE_EXCEPTIONS";

	public static final String SERVER = "localhost";

	@Autowired
	protected ApplicationContext context;
	@Resource
	public Environment env;

	public AppContextCore() {
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
