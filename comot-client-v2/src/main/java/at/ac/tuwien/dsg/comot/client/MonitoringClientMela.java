package at.ac.tuwien.dsg.comot.client;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.client.stub.MelaStub;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;


public class MonitoringClientMela implements MonitoringClient{

	private final Logger log = LoggerFactory.getLogger(MonitoringClientMela.class);
	
	protected MelaStub mela;
	
	public MonitoringClientMela(){
		this.mela = new MelaStub();
	}
	
	// TODO
	
	@PreDestroy
	public void cleanup() {
		log.info("closing mela client");
		mela.close();
	}

	@Override
	public void setHost(String host) {
		mela.setHost(host);
	}

	@Override
	public void setPort(int port) {
		mela.setPort(port);
	}
}
