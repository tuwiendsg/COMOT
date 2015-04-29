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
package at.ac.tuwien.dsg.comot.m.ui;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.ui.model.Error;

@Provider
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
public class ComotExceptionMapper implements ExceptionMapper<Exception> {

	private static final Logger log = LoggerFactory.getLogger(ComotExceptionMapper.class);
	protected static final String NAME = "COMOT";

	@Context
	private HttpServletRequest req;

	@Override
	public Response toResponse(Exception e) {

		if (e instanceof WebApplicationException) {
			WebApplicationException we = (WebApplicationException) e;
			log.warn("REST interface exception", e);
			return Response
					.status(we.getResponse().getStatus())
					.entity(resolve(req, new Error()))
					.build();

		} else if (e.getClass().equals(ComotIllegalArgumentException.class)) {
			log.warn("Wrong user input: {}", e.getMessage());
			return Response.status(404).entity(resolve(req, new Error(e.getMessage()))).build();

		} else if (e.getClass().equals(EpsException.class)) {
			EpsException ce = (EpsException) e;

			if (ce.isClientError()) {
				log.warn("Core service CLIENT ERROR", e);
			} else {
				log.warn("Core service SERVER ERROR", e);
			}
			return Response.status(ce.getCode()).entity(resolve(req, new Error(ce.getMsg(), ce.getComponentName())))
					.build();

		} else if (e.getClass().equals(ComotException.class)) {
			log.error("Something bad happened: {}", e);
			return Response.serverError().entity(resolve(req, new Error())).build();

		} else if (e.getClass().equals(ComotLifecycleException.class)) {
			log.warn("Lifecycle exception: {}", e.getMessage());
			return Response.status(404).entity(resolve(req, new Error(e.getMessage()))).build();

		} else {
			log.error("Wut? {}", e);
			return Response.serverError().entity(resolve(req, new Error())).build();
		}
	}

	private Object resolve(HttpServletRequest req, Error error) {

		String accept = req.getHeader("Accept");

		if (accept.equalsIgnoreCase("text/plain") || accept.equals("*/*")) {
			return error.toString();
		} else {
			return error;
		}
	}
}
