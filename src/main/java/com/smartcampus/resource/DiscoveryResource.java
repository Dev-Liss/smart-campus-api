package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("apiVersion", "1.0");
        info.put("description", "Smart Campus Sensor & Room Management API");
        info.put("contact", "admin@smartcampus.com");
        info.put("endpoints", endpoints);

        return Response.ok(info).build();
    }
}
