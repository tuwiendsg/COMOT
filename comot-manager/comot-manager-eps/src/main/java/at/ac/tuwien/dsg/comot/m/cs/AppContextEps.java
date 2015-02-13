package at.ac.tuwien.dsg.comot.m.cs;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.MelaClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;

@Configuration
// @ImportResource({"classpath:spring/dozerBean.xml"})
// @PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.cs" })
public class AppContextEps {

	public static final String SALSA_IP = "128.130.172.215";
	public static final String MELA_IP = "128.130.172.215";
	public static final String RSYBL_IP = "128.130.172.215";
	public static final int MELA_PORT = 8180;
	public static final int SALSA_PORT = 8380;
	public static final int RSYBL_PORT = 8020;

	@Resource
	public Environment env;

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
		return new RsyblClient(RSYBL_IP, RSYBL_PORT);
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
