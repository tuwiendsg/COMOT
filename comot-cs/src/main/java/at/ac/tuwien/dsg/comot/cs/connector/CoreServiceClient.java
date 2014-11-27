package at.ac.tuwien.dsg.comot.cs.connector;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;

public abstract class CoreServiceClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected static final String DEF_HOST = "localhost";
	protected static final int DEF_PORT = 8080;
	protected static String name;

	protected Client client;
	protected String baseUri;

	protected String host;
	protected int port;
	protected String basePath;

	public CoreServiceClient(String host, int port, String basePath) {
		super();
		this.host = host;
		this.port = port;
		this.basePath = basePath;

		client = ClientBuilder.newClient();
	}

	protected String getBaseUri() {
		return baseUri = "http://" + host + ":" + port + basePath;
	}

	public void close() {
		client.close();
	}


	protected void processResponseStatus(Response response) throws CoreServiceException {

		int code = response.getStatus();
		int hundreds = code / 100;
		String msg;

		switch (hundreds) {
		case 1:
			break;
		case 2:
			log.trace(name + "HTTP response status code={}", code);
			break;
		case 3:
			break;
		case 4:
			msg = response.readEntity(String.class);
			log.trace(name + "HTTP response status code={} , message='{}' ", code, msg);
			throw new CoreServiceException(code, msg);
		case 5:
			msg = response.readEntity(String.class);
			log.trace(name + "HTTP response status code={} , message='{}' ", code, msg);
			throw new CoreServiceException(code, msg);
		}

	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

}
