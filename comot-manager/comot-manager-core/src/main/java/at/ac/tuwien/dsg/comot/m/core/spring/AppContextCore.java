package at.ac.tuwien.dsg.comot.m.core.spring;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;

@Configuration
@EnableTransactionManagement
// @PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.core" })
@Import({ AppContextEps.class, AppContextServrec.class })
@EnableAsync
public class AppContextCore {

	public static final Logger log = LoggerFactory.getLogger(AppContextCore.class);

	public static final String INSERT_INIT_DATA = "INSERT_INIT_DATA";

	public static final String EXCHANGE_LIFE_CYCLE = "EXCHANGE_LIFE_CYCLE";
	public static final String EXCHANGE_CUSTOM_EVENT = "EXCHANGE_CUSTOM_EVENT";

	public static final String SERVER = "localhost";

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

}
