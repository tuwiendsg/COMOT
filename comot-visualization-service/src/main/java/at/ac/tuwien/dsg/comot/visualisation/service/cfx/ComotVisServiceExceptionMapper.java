package at.ac.tuwien.dsg.comot.visualisation.service.cfx;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 2/14/14.
 */
@Provider
@Component
public class ComotVisServiceExceptionMapper implements ExceptionMapper<Exception> {

    static final Logger log = LoggerFactory.getLogger(ComotVisServiceExceptionMapper.class);

    @PostConstruct
    public void init() {
        System.out.println("initialized");
    }

    public Response toResponse(Exception exception) {
//        return Response.serverError().entity("Internal Error: " + exception.getMessage()).build();
        log.error(exception.getMessage(), exception);
        return Response.serverError().entity("Internal Error: " + exception).build();
    }

    class ErrorResponse {

        private int code;

        private String message;

        private String documentationUri;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getDocumentationUri() {
            return documentationUri;
        }

        public ErrorResponse withDocumentationUri(final String documentationUri) {
            this.documentationUri = documentationUri;
            return this;
        }

        public ErrorResponse withCode(final int code) {
            this.code = code;
            return this;
        }

        public ErrorResponse withMessage(final String message) {
            this.message = message;
            return this;
        }

    }
}
