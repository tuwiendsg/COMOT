package at.ac.tuwien.dsg.comot.ui;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.cs.ControlClientRsybl;
import at.ac.tuwien.dsg.comot.cs.DeploymentClientSalsa;
import at.ac.tuwien.dsg.comot.cs.MonitoringClientMela;
import at.ac.tuwien.dsg.comot.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;

@Configuration
// @PropertySource({ "classpath:spring/properties/application.properties" })
 @Import(at.ac.tuwien.dsg.comot.core.spring.ApplicationContext.class)
// @ImportResource({"classpath:WEB-INF/beans.xml"})
@ComponentScan({ "at.ac.tuwien.dsg.comot" })
public class ApplicationContext {

	@Resource
	public Environment env;

	public static final String SALSA_IP = "128.130.172.215";
	public static final String MELA_IP = "128.130.172.216";
	public static final int MELA_PORT = 8180;
	public static final int SALSA_PORT = 8080;

	@Bean
	public SalsaClient salsaClient() {
		return new SalsaClient(SALSA_IP, SALSA_PORT);
	}

	@Bean
	public MelaClient melaClient() {
		return new MelaClient(MELA_IP, MELA_PORT);
	}

	@Bean
	public RsyblClient rsyblClient() {
		return new RsyblClient("128.130.172.216", 8280);
	}

	@Bean
	public DeploymentClient deploymentClient() {
		return new DeploymentClientSalsa();
	}

	@Bean
	public MonitoringClient monitoringClient() {
		return new MonitoringClientMela();
	}

	@Bean
	public ControlClient controlClient() {
		return new ControlClientRsybl();
	}

}
