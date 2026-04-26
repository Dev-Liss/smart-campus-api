package com.smartcampus.exception;

import com.smartcampus.model.ApiErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException e) {
        return Response.status(403)
                .entity(new ApiErrorResponse("Forbidden", e.getMessage()))
                .type(APPLICATION_JSON)
                .build();
    }
}
