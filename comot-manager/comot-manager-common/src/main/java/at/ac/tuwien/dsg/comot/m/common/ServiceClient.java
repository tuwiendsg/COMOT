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
package at.ac.tuwien.dsg.comot.m.common;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;

public abstract class ServiceClient {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected String name;
	protected String ln;

	protected Client client;
	protected URI baseUri;

	public ServiceClient() {

	}

	public ServiceClient(String name, URI baseUri) {
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
			log.trace(ln + "HTTP response status code={}", code);
			break;
		case 3:
			break;
		case 4:
			msg = response.readEntity(String.class);
			log.trace(ln + "HTTP response status code={} , message='{}' ", code, msg);
			throw new EpsException(code, msg, name);
		case 5:
			msg = response.readEntity(String.class);
			log.trace(ln + "HTTP response status code={} , message='{}' ", code, msg);
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
