package at.ac.tuwien.dsg.comot.ui;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;

@Provider
public class ComotExceptionMapper implements ExceptionMapper<Exception> {

	private static final Logger log = LoggerFactory.getLogger(ComotExceptionMapper.class);
	protected static final String NAME = "COMOT";

	@Override
	public Response toResponse(Exception e) {

		if (e.getClass().equals(ComotIllegalArgumentException.class)) {
			log.warn("Wrong user input: {}", e.getMessage());
			return Response.status(404).entity(new Error(e.getMessage())).build();

		} else if (e.getClass().equals(CoreServiceException.class)) {
			CoreServiceException ce = (CoreServiceException) e;

			if (ce.isClientError()) {
				log.warn("Core service CLIENT ERROR", e);
			} else {
				log.warn("Core service SERVER ERROR", e);
			}
			return Response.status(ce.getCode()).entity(new Error(ce.getMsg(), ce.getComponentName()))
					.build();

		} else if (e.getClass().equals(ComotException.class)) {
			log.error("Something bad happened: {}", e);
			return Response.serverError().entity(new Error()).build();

		} else if (e instanceof WebApplicationException) {
			WebApplicationException we = (WebApplicationException) e;
			log.warn("REST interface exception", e);
			return Response
					.status(we.getResponse().getStatus())
					.entity(new Error())
					.build();

		} else {
			log.error("Wut? {}", e);
			return Response.serverError().entity(new Error()).build();
		}
	}
}
