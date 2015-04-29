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
package at.ac.tuwien.dsg.comot.m.cs.test;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.eps.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.cs.ControlClientRsybl;
import at.ac.tuwien.dsg.comot.m.cs.DeploymentClientSalsa;
import at.ac.tuwien.dsg.comot.m.cs.MonitoringClientMela;
import at.ac.tuwien.dsg.comot.m.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;

@Configuration
@PropertySource({ "classpath:properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.cs" })
public class AppContextEpsClients {

	@Resource
	public Environment env;

	@Autowired
	protected SalsaClient salsaClient;
	@Autowired
	protected MelaClient melaClient;
	@Autowired
	protected RsyblClient rsyblClient;

	@Bean
	public SalsaClient salsaClient() throws URISyntaxException {
		return new SalsaClient(new URI(env.getProperty("uri.deployemnt")));
	}

	@Bean
	public MelaClient melaClient() throws URISyntaxException {
		return new MelaClient(new URI(env.getProperty("uri.monitoring")));
	}

	@Bean
	public RsyblClient rsyblClient() throws URISyntaxException {
		return new RsyblClient(new URI(env.getProperty("uri.controller")));
	}

	@Bean
	public DeploymentClient deploymentClient() {
		return new DeploymentClientSalsa(salsaClient);
	}

	@Bean
	public MonitoringClient monitoringClient() {
		return new MonitoringClientMela(melaClient);
	}

	@Bean
	public ControlClient controlClient() {
		return new ControlClientRsybl(rsyblClient);
	}

}
