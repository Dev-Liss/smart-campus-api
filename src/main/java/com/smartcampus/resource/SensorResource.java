package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ApiErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

@Path("/sensors")
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors() {
        return Response.ok(dataStore.getSensors().values()).build();
    }

    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Not Found", "Sensor not found: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updates) {
        Sensor existing = dataStore.getSensors().get(sensorId);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Not Found", "Sensor not found: " + sensorId))
                    .build();
        }

        if (updates.getType() != null) {
            existing.setType(updates.getType());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        existing.setCurrentValue(updates.getCurrentValue());

        return Response.ok(existing).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Not Found", "Sensor not found: " + sensorId))
                    .build();
        }

        dataStore.getSensors().remove(sensorId);

        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        return Response.noContent().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // --- Validation ---
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Bad Request", "Sensor type must not be blank"))
                    .build();
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Bad Request", "Sensor status must not be blank"))
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Bad Request", "Sensor roomId must not be blank"))
                    .build();
        }

        // --- Room existence check ---
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room not found: " + sensor.getRoomId());
        }

        // --- Persist ---
        String id = UUID.randomUUID().toString();
        sensor.setId(id);
        dataStore.getSensors().put(id, sensor);
        room.getSensorIds().add(id);

        return Response.created(URI.create("/api/v1/sensors/" + id))
                .entity(sensor)
                .build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId, dataStore);
    }
}
