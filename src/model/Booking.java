package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Booking {
    private final String bookingId;
    private final Guest guest;
    private final Room room;

    private final LocalDate checkIn;
    private final LocalDate checkOut;

    private final double pricePerNight;
    private boolean cancelled;

    public Booking(
            String bookingId,
            Guest guest,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut) {

        this(
            bookingId,
            guest,
            room,
            checkIn,
            checkOut,
            room == null ? 0 : room.getPricePerNight(),
            false
        );
    }

    public Booking(
            String bookingId,
            Guest guest,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            double pricePerNight,
            boolean cancelled) {

        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException(
                "Booking ID cannot be empty."
            );
        }

        if (guest == null || room == null) {
            throw new IllegalArgumentException(
                "A booking requires a guest and a room."
            );
        }

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException(
                "Check-in and check-out dates are required."
            );
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                "Check-out must be after check-in."
            );
        }

        this.bookingId = bookingId.trim().toUpperCase(Locale.ROOT);
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;

        if (!Double.isFinite(pricePerNight) || pricePerNight <= 0) {
            throw new IllegalArgumentException(
                "Booked price must be a positive, finite number."
            );
        }

        // Keep the price agreed when this booking was created.
        this.pricePerNight = pricePerNight;
        this.cancelled = cancelled;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double getTotalPrice() {
        return getNumberOfNights() * pricePerNight;
    }

    public void cancel() {
        cancelled = true;
    }

    public void displayInfo() {
        System.out.println("------------------------------");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Guest: " + guest.getName());
        System.out.println("Room: " + room.getRoomNumber());
        System.out.println("Room type: " + room.getRoomType());
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);
        System.out.println("Nights: " + getNumberOfNights());

        System.out.printf(
            Locale.ROOT, "Booked total: $%.2f%n", getTotalPrice()
        );

        System.out.println(
            "Status: " + (cancelled ? "Cancelled" : "Confirmed")
        );
    }
}
