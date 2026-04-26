package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore instance = new DataStore();

    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        // --- Seed Rooms ---
        Room room1 = new Room("room-1", "Library Quiet Study", 50);
        Room room2 = new Room("room-2", "Innovation Lab", 30);

        // --- Seed Sensors ---
        Sensor sensor1 = new Sensor("sensor-1", "Temperature", "ACTIVE", 22.5, "room-1");
        Sensor sensor2 = new Sensor("sensor-2", "Humidity", "ACTIVE", 55.0, "room-2");

        // Link sensor IDs to their rooms
        room1.getSensorIds().add(sensor1.getId());
        room2.getSensorIds().add(sensor2.getId());

        // Populate maps
        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);

        // Initialise empty reading lists for each sensor
        sensorReadings.put(sensor1.getId(), new ArrayList<>());
        sensorReadings.put(sensor2.getId(), new ArrayList<>());
    }

    public static DataStore getInstance() {
        return instance;
    }

    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    public ConcurrentHashMap<String, Sensor> getSensors() {
        return sensors;
    }

    public ConcurrentHashMap<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}
