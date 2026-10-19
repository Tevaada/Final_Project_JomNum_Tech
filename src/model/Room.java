package model;

public abstract class Room {

    private final String roomNumber;
    private double pricePerNight;

    public Room(String roomNumber, double pricePerNight) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException(
                "Room number cannot be empty."
            );
        }

        this.roomNumber = roomNumber.trim();
        setPricePerNight(pricePerNight);
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public final void setPricePerNight(double pricePerNight) {
        if (!Double.isFinite(pricePerNight) || pricePerNight <= 0) {
            throw new IllegalArgumentException(
                "Price per night must be a positive, finite number."
            );
        }

        this.pricePerNight = pricePerNight;
    }

    public abstract String getRoomType();

    public abstract String getDescription();

    public void displayInfo() {
        System.out.println("------------------------------");
        System.out.println("Room number: " + roomNumber);
        System.out.println("Room type: " + getRoomType());
        System.out.printf("Price per night: $%.2f%n", pricePerNight);
        System.out.println("Description: " + getDescription());
    }
}
