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
package at.ac.tuwien.dsg.comot.m.info;

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

import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;

@Provider
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
public class InfoExceptionMapper implements ExceptionMapper<Exception> {

	private static final Logger LOG = LoggerFactory.getLogger(InfoExceptionMapper.class);
	protected static final String NAME = "COMOT";

	@Context
	private HttpServletRequest req;

	@Override
	public Response toResponse(Exception e) {

		if (e instanceof WebApplicationException) {
			WebApplicationException we = (WebApplicationException) e;
			LOG.warn("REST interface exception", e);
			return Response
					.status(we.getResponse().getStatus())
					.entity(e.getMessage())
					.build();

		} else if (e.getClass().equals(ComotIllegalArgumentException.class)) {
			LOG.warn("Wrong user input: {}", e.getMessage());
			return Response.status(404).entity(e.getMessage()).build();

		} else {
			LOG.error("{}", e);
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

}
