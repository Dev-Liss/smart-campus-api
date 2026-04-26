package com.smartcampus.exception;

import com.smartcampus.model.ApiErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException e) {
        return Response.status(409)
                .entity(new ApiErrorResponse("Conflict", e.getMessage()))
                .type(APPLICATION_JSON)
                .build();
    }
}
