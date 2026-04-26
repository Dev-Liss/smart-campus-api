package com.smartcampus.resource;

import com.smartcampus.model.ApiErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/rooms")
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        return Response.ok(dataStore.getRooms().values()).build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Room not found", roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Validation failed", "Request body must not be null"))
                    .build();
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Validation failed", "Room name must not be blank"))
                    .build();
        }
        if (room.getCapacity() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Validation failed", "Capacity must be greater than 0"))
                    .build();
        }

        if (room.getId() == null || room.getId().trim().isEmpty()) {
            room.setId(UUID.randomUUID().toString());
        }
        dataStore.getRooms().put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRoom(@PathParam("roomId") String roomId, Room updates) {
        Room existingRoom = dataStore.getRooms().get(roomId);
        if (existingRoom == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Room not found", roomId))
                    .build();
        }
        if (updates == null || updates.getName() == null || updates.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Validation failed", "Room name must not be blank"))
                    .build();
        }
        if (updates.getCapacity() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorResponse("Validation failed", "Capacity must be greater than 0"))
                    .build();
        }

        existingRoom.setName(updates.getName());
        existingRoom.setCapacity(updates.getCapacity());
        return Response.ok(existingRoom).build();
    }

    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiErrorResponse("Room not found", roomId))
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room has active sensors");
        }
        dataStore.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
