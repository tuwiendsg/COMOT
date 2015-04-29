/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.InformationClientRest;
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
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;

@Configuration
@EnableTransactionManagement
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.core", "at.ac.tuwien.dsg.comot.m.adapter" })
@Import({ AppContextEps.class, AppContextServrec.class })
@EnableAsync
public class AppContextCore {

	public static final Logger log = LoggerFactory.getLogger(AppContextCore.class);

	public static final String INSERT_INIT_DATA = "INSERT_INIT_DATA";

	@Autowired
	protected ApplicationContext context;
	@Resource
	public Environment env;

	public AppContextCore() {
		super();
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(env.getProperty("uri.broker.host"));
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
	public InformationClient informationClient() throws URISyntaxException {
		return new InformationClient(new InformationClientRest(new URI(env.getProperty("uri.information"))));
	}

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
