package service;

import exception.BookingException;
import model.Booking;
import model.Guest;
import model.Room;

import java.time.LocalDate;
import java.util.ArrayList;

public class BookingService {
    private final ArrayList<Booking> bookings = new ArrayList<>();

    private final RoomService roomService;
    private final GuestService guestService;

    public BookingService(
            RoomService roomService,
            GuestService guestService) {

        this.roomService = roomService;
        this.guestService = guestService;
    }

    public void createBooking(
            String bookingId,
            String guestId,
            String roomNumber,
            LocalDate checkIn,
            LocalDate checkOut) {

        if (bookingId == null || bookingId.isBlank()) {
            throw new BookingException("Booking ID cannot be empty.");
        }

        if (findBooking(bookingId) != null) {
            throw new BookingException("Booking ID already exists.");
        }

        Guest guest = guestService.findGuest(guestId);

        if (guest == null) {
            throw new BookingException(
                "Guest not found. Register the guest first."
            );
        }

        Room room = roomService.findRoom(roomNumber);

        if (room == null) {
            throw new BookingException("Room not found.");
        }

        if (checkIn == null || checkOut == null) {
            throw new BookingException("Both dates are required.");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException(
                "Check-out must be after check-in."
            );
        }

        if (!isRoomAvailable(roomNumber, checkIn, checkOut)) {
            throw new BookingException(
                "This room is already booked for those dates."
            );
        }

        Booking booking = new Booking(
            bookingId, guest, room, checkIn, checkOut
        );

        bookings.add(booking);
    }

    public void restoreBooking(
            String bookingId,
            String guestId,
            String roomNumber,
            LocalDate checkIn,
            LocalDate checkOut,
            double pricePerNight,
            boolean cancelled) {

        if (bookingId == null || bookingId.isBlank()) {
            throw new BookingException("Booking ID cannot be empty.");
        }

        if (findBooking(bookingId) != null) {
            throw new BookingException("Booking ID already exists.");
        }

        Guest guest = guestService.findGuest(guestId);
        Room room = roomService.findRoom(roomNumber);

        if (guest == null) {
            throw new BookingException("Referenced guest was not found.");
        }

        if (room == null) {
            throw new BookingException("Referenced room was not found.");
        }

        if (checkIn == null || checkOut == null) {
            throw new BookingException("Both dates are required.");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException(
                "Check-out must be after check-in."
            );
        }

        if (!cancelled
                && !isRoomAvailable(roomNumber, checkIn, checkOut)) {
            throw new BookingException(
                "Confirmed booking overlaps another confirmed booking."
            );
        }

        bookings.add(new Booking(
            bookingId,
            guest,
            room,
            checkIn,
            checkOut,
            pricePerNight,
            cancelled
        ));
    }

    public boolean isRoomAvailable(
            String roomNumber,
            LocalDate checkIn,
            LocalDate checkOut) {

        for (Booking booking : bookings) {
            if (booking.isCancelled()) {
                continue;
            }

            boolean sameRoom = booking.getRoom()
                .getRoomNumber()
                .equalsIgnoreCase(roomNumber.trim());

            boolean datesOverlap =
                checkIn.isBefore(booking.getCheckOut())
                && checkOut.isAfter(booking.getCheckIn());

            if (sameRoom && datesOverlap) {
                return false;
            }
        }

        return true;
    }

    public void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Booking booking : bookings) {
            booking.displayInfo();
        }
    }

    public Booking findBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new BookingException("Booking ID cannot be empty.");
        }

        for (Booking booking : bookings) {
            if (booking.getBookingId()
                    .equalsIgnoreCase(bookingId.trim())) {

                return booking;
            }
        }

        return null;
    }

    public void cancelBooking(String bookingId) {
        Booking booking = findBooking(bookingId);

        if (booking == null) {
            throw new BookingException("Booking not found.");
        }

        if (booking.isCancelled()) {
            throw new BookingException(
                "This booking is already cancelled."
            );
        }

        booking.cancel();
    }

    public void deleteCancelledBooking(String bookingId) {
        Booking booking = findBooking(bookingId);

        if (booking == null) {
            throw new BookingException("Booking not found.");
        }

        if (!booking.isCancelled()) {
            throw new BookingException(
                "Only cancelled bookings can be deleted."
            );
        }

        bookings.remove(booking);
    }

    public boolean hasBookingsForRoom(String roomNumber) {
        for (Booking booking : bookings) {
            if (booking.getRoom().getRoomNumber()
                    .equalsIgnoreCase(roomNumber.trim())) {

                return true;
            }
        }

        return false;
    }

    public boolean hasBookingsForGuest(String guestId) {
        for (Booking booking : bookings) {
            if (booking.getGuest().getGuestId()
                    .equalsIgnoreCase(guestId.trim())) {

                return true;
            }
        }

        return false;
    }

    public ArrayList<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }
}
