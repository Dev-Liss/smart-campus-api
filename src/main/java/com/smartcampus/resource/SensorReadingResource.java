package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore;

    public SensorReadingResource(String sensorId, DataStore dataStore) {
        this.sensorId = sensorId;
        this.dataStore = dataStore;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Not Found", "Sensor not found: " + sensorId))
                    .build();
        }

        List<SensorReading> readings = dataStore.getSensorReadings()
                .getOrDefault(sensorId, Collections.emptyList());
        return Response.ok(readings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Not Found", "Sensor not found: " + sensorId))
                    .build();
        }

        if ("MAINTENANCE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor in maintenance");
        }

        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        dataStore.getSensorReadings()
                .computeIfAbsent(sensorId, k -> new java.util.ArrayList<>())
                .add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading)
                .build();
    }
}
