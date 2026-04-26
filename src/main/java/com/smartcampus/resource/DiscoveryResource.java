package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Smart Campus API");
        info.put("version", "1.0-SNAPSHOT");
        info.put("status", "running");
        return Response.ok(info).build();
    }
}
