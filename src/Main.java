import exception.BookingException;

import service.RoomService;
import service.GuestService;
import service.BookingService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import model.HotelData;
import util.FileManager;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
    HotelData hotelData;

    try {
        hotelData = FileManager.load();
        System.out.println("Hotel data is ready.");

    } catch (IOException e) {
        System.out.println(
            "Cannot load hotel data: " + e.getMessage()
        );

        System.out.println(
            "Startup stopped to protect the existing data file."
        );

        return;
    }

    Scanner scanner = new Scanner(System.in);

    RoomService roomService = hotelData.getRoomService();
    GuestService guestService = hotelData.getGuestService();
    BookingService bookingService = hotelData.getBookingService();

    boolean running = true;

    while (running) {
        System.out.println("\n================================");
        System.out.println("      HOTEL BOOKING SYSTEM");
        System.out.println("================================");
        System.out.println("1. Manage Rooms");
        System.out.println("2. Manage Guests");
        System.out.println("3. Create Booking");
        System.out.println("4. View Bookings");
        System.out.println("5. Cancel Booking");
        System.out.println("0. Save and Exit");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> manageRooms(
                scanner, roomService, bookingService, hotelData
            );

            case "2" -> manageGuests(
                scanner, guestService, bookingService, hotelData
            );

            case "3" -> {
                if (createBooking(scanner, bookingService)) {
                    saveData(hotelData, false);
                }
            }

            case "4" -> bookingService.viewBookings();

            case "5" -> {
                if (cancelBooking(scanner, bookingService)) {
                    saveData(hotelData, false);
                }
            }

            case "0" -> {
                if (saveData(hotelData, true)) {
                    running = false;
                }
            }

            default -> {
                System.out.println(
                    "Please choose a number from 0 to 5."
                );

                continue;
            }
        }

    }

    scanner.close();

}
    // ==================== ROOM MANAGEMENT ====================

    private static void manageRooms(
            Scanner scanner,
            RoomService roomService,
            BookingService bookingService,
            HotelData hotelData) {

        boolean managing = true;

        while (managing) {
            System.out.println("\n===== MANAGE ROOMS =====");
            System.out.println("1. Add Room");
            System.out.println("2. View Rooms");
            System.out.println("3. Update Room Price");
            System.out.println("4. Delete Room");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Room number: ");
                        String roomNumber = scanner.nextLine().trim();

                        System.out.println("1. Standard");
                        System.out.println("2. Deluxe");
                        System.out.print("Choose room type: ");
                        String type = scanner.nextLine().trim();

                        double price = readPrice(scanner);

                        roomService.addRoom(roomNumber, type, price);

                        System.out.println("Room added successfully.");
                        saveData(hotelData, false);
                    }

                    case "2" -> roomService.viewRooms();

                    case "3" -> {
                        System.out.print("Room number to update: ");
                        String roomNumber = scanner.nextLine().trim();

                        double newPrice = readPrice(scanner);

                        roomService.updateRoomPrice(roomNumber, newPrice);

                        System.out.println(
                            "Room price updated successfully."
                        );
                        saveData(hotelData, false);
                    }

                    case "4" -> {
                        System.out.print("Room number to delete: ");
                        String roomNumber = scanner.nextLine().trim();

                        System.out.print("Type YES to confirm deletion: ");
                        String confirmation = scanner.nextLine().trim();

                        if (confirmation.equalsIgnoreCase("YES")) {
                            if (bookingService.hasBookingsForRoom(
                                    roomNumber)) {

                                throw new IllegalArgumentException(
                                    "Cannot delete a room referenced by a booking."
                                );
                            }

                            roomService.deleteRoom(roomNumber);

                            System.out.println(
                                "Room deleted successfully."
                            );
                            saveData(hotelData, false);
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    }

                    case "0" -> managing = false;

                    default -> System.out.println(
                        "Please choose a number from 0 to 4."
                    );
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ==================== GUEST MANAGEMENT ====================

    private static void manageGuests(
            Scanner scanner,
            GuestService guestService,
            BookingService bookingService,
            HotelData hotelData) {

        boolean managing = true;

        while (managing) {
            System.out.println("\n===== MANAGE GUESTS =====");
            System.out.println("1. Add Guest");
            System.out.println("2. View Guests");
            System.out.println("3. Update Guest");
            System.out.println("4. Delete Guest");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Guest ID: ");
                        String guestId = scanner.nextLine().trim();

                        System.out.print("Guest name: ");
                        String name = scanner.nextLine().trim();

                        System.out.print("Phone number: ");
                        String phone = scanner.nextLine().trim();

                        guestService.addGuest(guestId, name, phone);

                        System.out.println("Guest added successfully.");
                        saveData(hotelData, false);
                    }

                    case "2" -> guestService.viewGuests();

                    case "3" -> {
                        System.out.print("Guest ID to update: ");
                        String guestId = scanner.nextLine().trim();

                        System.out.print("New name: ");
                        String newName = scanner.nextLine().trim();

                        System.out.print("New phone number: ");
                        String newPhone = scanner.nextLine().trim();

                        guestService.updateGuest(
                            guestId, newName, newPhone
                        );

                        System.out.println("Guest updated successfully.");
                        saveData(hotelData, false);
                    }

                    case "4" -> {
                        System.out.print("Guest ID to delete: ");
                        String guestId = scanner.nextLine().trim();

                        System.out.print("Type YES to confirm deletion: ");
                        String confirmation = scanner.nextLine().trim();

                        if (confirmation.equalsIgnoreCase("YES")) {
                            if (bookingService.hasBookingsForGuest(
                                    guestId)) {

                                throw new IllegalArgumentException(
                                    "Cannot delete a guest referenced by a booking."
                                );
                            }

                            guestService.deleteGuest(guestId);

                            System.out.println(
                                "Guest deleted successfully."
                            );
                            saveData(hotelData, false);
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    }

                    case "0" -> managing = false;

                    default -> System.out.println(
                        "Please choose a number from 0 to 4."
                    );
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ==================== CREATE BOOKING ====================

    private static boolean createBooking(
            Scanner scanner, BookingService bookingService) {

        System.out.println("\n===== CREATE BOOKING =====");

        try {
            System.out.print("Booking ID: ");
            String bookingId = scanner.nextLine().trim();

            System.out.print("Guest ID: ");
            String guestId = scanner.nextLine().trim();

            System.out.print("Room number: ");
            String roomNumber = scanner.nextLine().trim();

            LocalDate checkIn = readDate(
                scanner, "Check-in date (YYYY-MM-DD): "
            );

            LocalDate checkOut = readDate(
                scanner, "Check-out date (YYYY-MM-DD): "
            );

            bookingService.createBooking(
                bookingId,
                guestId,
                roomNumber,
                checkIn,
                checkOut
            );

            System.out.println("Booking created successfully.");

            bookingService.findBooking(bookingId).displayInfo();
            return true;

        } catch (BookingException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    // ==================== CANCEL BOOKING ====================

    private static boolean cancelBooking(
            Scanner scanner, BookingService bookingService) {

        System.out.println("\n===== CANCEL BOOKING =====");

        try {
            System.out.print("Booking ID: ");
            String bookingId = scanner.nextLine().trim();

            System.out.print("Type YES to confirm cancellation: ");
            String confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("YES")) {
                bookingService.cancelBooking(bookingId);

                System.out.println(
                    "Booking cancelled successfully."
                );
                return true;
            } else {
                System.out.println("Booking was not changed.");
                return false;
            }

        } catch (BookingException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private static boolean saveData(
            HotelData hotelData, boolean exiting) {

        try {
            FileManager.save(hotelData);

            if (exiting) {
                System.out.println("Data saved. Thank you. Goodbye!");
            } else {
                System.out.println("Data saved.");
            }

            return true;
        } catch (IOException e) {
            System.out.println(
                "Could not save data: " + e.getMessage()
            );
            System.out.println(
                "Your changes are still in memory. "
                + "Keep the program open and choose 0 to retry saving."
            );
            return false;
        }
    }

    // ==================== PRICE INPUT ====================

    private static double readPrice(Scanner scanner) {
        while (true) {
            System.out.print("Price per night: ");

            try {
                double price = Double.parseDouble(
                    scanner.nextLine().trim()
                );

                if (!Double.isFinite(price) || price <= 0) {
                    System.out.println(
                        "Please enter a positive, finite price."
                    );
                    continue;
                }

                return price;

            } catch (NumberFormatException e) {
                System.out.println(
                    "Please enter a number, such as 45.50."
                );
            }
        }
    }

    // ==================== DATE INPUT ====================

    private static LocalDate readDate(
            Scanner scanner, String prompt) {

        while (true) {
            System.out.print(prompt);

            try {
                return LocalDate.parse(
                    scanner.nextLine().trim()
                );

            } catch (DateTimeParseException e) {
                System.out.println(
                    "Enter a valid date, such as 2026-11-20."
                );
            }
        }
    }
}
