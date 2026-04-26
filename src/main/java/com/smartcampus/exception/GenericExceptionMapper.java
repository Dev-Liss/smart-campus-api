package com.smartcampus.exception;

import com.smartcampus.model.ApiErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GenericExceptionMapper", e);
        return Response.status(500)
                .entity(new ApiErrorResponse("Internal Server Error", "An unexpected error occurred."))
                .type(APPLICATION_JSON)
                .build();
    }
}
