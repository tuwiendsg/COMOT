package at.ac.tuwien.dsg.comot.m.cs.connector;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;

public abstract class CoreServiceClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected String name;
	protected String ln;

	protected Client client;
	protected URI baseUri;

	public CoreServiceClient() {

	}

	public CoreServiceClient(String name, URI baseUri) {
		super();
		setName(name);
		this.baseUri = baseUri;

		client = ClientBuilder.newClient();
	}

	public void close() {
		client.close();
	}

	protected void processResponseStatus(Response response) throws EpsException {

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
			throw new EpsException(code, msg, name);
		case 5:
			msg = response.readEntity(String.class);
			log.trace(name + "HTTP response status code={} , message='{}' ", code, msg);
			throw new EpsException(code, msg, name);
		}

	}

	protected void setName(String name) {
		this.name = name;
		ln = "[" + name + "] ";
	}

	public URI getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(URI baseUri) {
		this.baseUri = baseUri;
	}

	public String getHost() {
		return baseUri.getHost();
	}

}
