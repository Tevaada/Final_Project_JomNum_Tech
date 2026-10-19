package model;

public class DeluxeRoom extends Room {

    public DeluxeRoom(String roomNumber, double pricePerNight) {
        super(roomNumber, pricePerNight);
    }

    @Override
    public String getRoomType() {
        return "Deluxe";
    }

    @Override
    public String getDescription() {
        return "A spacious room with a king-size bed, Wi-Fi, and a balcony.";
    }
}
