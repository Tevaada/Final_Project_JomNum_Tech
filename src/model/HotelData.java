package model;

import service.RoomService;
import service.GuestService;
import service.BookingService;

public class HotelData {

    private final RoomService roomService;
    private final GuestService guestService;
    private final BookingService bookingService;

    public HotelData() {
        this(true);
    }

    public HotelData(boolean includeSampleRooms) {
        roomService = new RoomService(includeSampleRooms);
        guestService = new GuestService();

        bookingService = new BookingService(
            roomService, guestService
        );
    }

    public RoomService getRoomService() {
        return roomService;
    }

    public GuestService getGuestService() {
        return guestService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }
}
