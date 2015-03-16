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

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
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
