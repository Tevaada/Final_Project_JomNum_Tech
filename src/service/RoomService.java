package service;

import model.Room;
import model.StandardRoom;
import model.DeluxeRoom;

import java.util.ArrayList;

public class RoomService {
    private final ArrayList<Room> rooms = new ArrayList<>();

    public RoomService() {
        this(true);
    }

    public RoomService(boolean includeSampleRooms) {
        if (includeSampleRooms) {
            rooms.add(new StandardRoom("101", 30.00));
            rooms.add(new StandardRoom("102", 35.00));
            rooms.add(new DeluxeRoom("201", 60.00));
        }
    }

    public void addRoom(String roomNumber, String type, double price) {
        if (findRoom(roomNumber) != null) {
            throw new IllegalArgumentException(
                "Room number already exists."
            );
        }

        Room room;

        switch (type) {
            case "1" -> room = new StandardRoom(roomNumber, price);
            case "2" -> room = new DeluxeRoom(roomNumber, price);

            default -> throw new IllegalArgumentException(
                "Room type must be 1 or 2."
            );
        }

        rooms.add(room);
    }

    public void viewRooms() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms available in the system.");
            return;
        }

        for (Room room : rooms) {
            room.displayInfo();
        }
    }

    public Room findRoom(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException(
                "Room number cannot be empty."
            );
        }

        String number = roomNumber.trim();

        for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(number)) {
                return room;
            }
        }

        return null;
    }

    public void updateRoomPrice(String roomNumber, double newPrice) {
        Room room = requireRoom(roomNumber);
        room.setPricePerNight(newPrice);
    }

    public void deleteRoom(String roomNumber) {
        Room room = requireRoom(roomNumber);
        rooms.remove(room);
    }

    public ArrayList<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    private Room requireRoom(String roomNumber) {
        Room room = findRoom(roomNumber);

        if (room == null) {
            throw new IllegalArgumentException("Room not found.");
        }

        return room;
    }
}
