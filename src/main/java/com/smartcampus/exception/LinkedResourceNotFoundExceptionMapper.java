package com.smartcampus.exception;

import com.smartcampus.model.ApiErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {
        return Response.status(422)
                .entity(new ApiErrorResponse("Unprocessable Entity", e.getMessage()))
                .type(APPLICATION_JSON)
                .build();
    }
}
