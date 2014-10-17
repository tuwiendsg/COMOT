package at.ac.tuwien.dsg.comot.client.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MelaStub extends CoreServiceStub{

	protected static final String DEF_BASE_PATH = ""; // TODO
	
	public MelaStub() {
		this(DEF_HOST, DEF_PORT);
	}

	public MelaStub(String host) {
		this(host, DEF_PORT, DEF_BASE_PATH);
	}

	public MelaStub(String host, int port) {
		this(host, port, DEF_BASE_PATH);
	}
	
	public MelaStub(String host, int port, String basePath) {
		super(host, port, basePath);
	}

	private static final Logger log = LoggerFactory.getLogger(MelaStub.class);

	// TODO
	
	
}
