package util;

import model.Booking;
import model.Guest;
import model.HotelData;
import model.Room;
import service.BookingService;
import service.GuestService;
import service.RoomService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class FileManager {

    private static final Path DATA_DIRECTORY =
        Path.of("data").toAbsolutePath();
    private static final Path ROOMS_FILE =
        DATA_DIRECTORY.resolve("rooms.csv");
    private static final Path GUESTS_FILE =
        DATA_DIRECTORY.resolve("guests.csv");
    private static final Path BOOKINGS_FILE =
        DATA_DIRECTORY.resolve("bookings.csv");
    private static final List<String> ROOM_HEADER = List.of(
        "roomNumber", "type", "pricePerNight"
    );
    private static final List<String> GUEST_HEADER = List.of(
        "guestId", "name", "phone"
    );
    private static final List<String> BOOKING_HEADER = List.of(
        "bookingId", "guestId", "roomNumber", "checkIn",
        "checkOut", "pricePerNight", "cancelled"
    );

    private FileManager() {
    }

    public static HotelData load() throws IOException {

        Files.createDirectories(DATA_DIRECTORY);

        boolean roomsExist = Files.exists(ROOMS_FILE);
        boolean guestsExist = Files.exists(GUESTS_FILE);
        boolean bookingsExist = Files.exists(BOOKINGS_FILE);
        int existingCsvFiles = (roomsExist ? 1 : 0)
            + (guestsExist ? 1 : 0)
            + (bookingsExist ? 1 : 0);

        if (existingCsvFiles == 3) {
            return loadCsvData(
                ROOMS_FILE, GUESTS_FILE, BOOKINGS_FILE
            );
        }

        if (existingCsvFiles != 0) {
            throw new IOException(
                "Incomplete CSV storage. rooms.csv, guests.csv, and "
                + "bookings.csv must all exist. No files were changed."
            );
        }

        HotelData firstRunData = new HotelData();
        save(firstRunData);
        System.out.println("Created new CSV storage with sample rooms.");
        return firstRunData;
    }

    public static void save(HotelData hotelData) throws IOException {
        if (hotelData == null) {
            throw new IllegalArgumentException(
                "Hotel data cannot be null."
            );
        }

        Files.createDirectories(DATA_DIRECTORY);

        CsvUtil.write(ROOMS_FILE, roomRecords(hotelData));
        CsvUtil.write(GUESTS_FILE, guestRecords(hotelData));
        CsvUtil.write(BOOKINGS_FILE, bookingRecords(hotelData));
    }

    private static HotelData loadCsvData(
            Path roomsFile,
            Path guestsFile,
            Path bookingsFile) throws IOException {

        HotelData loadedData = new HotelData(false);
        RoomService rooms = loadedData.getRoomService();
        GuestService guests = loadedData.getGuestService();
        BookingService bookings = loadedData.getBookingService();

        ArrayList<ArrayList<String>> roomRows =
            readAndValidateHeader(roomsFile, ROOM_HEADER);
        ArrayList<ArrayList<String>> guestRows =
            readAndValidateHeader(guestsFile, GUEST_HEADER);
        ArrayList<ArrayList<String>> bookingRows =
            readAndValidateHeader(bookingsFile, BOOKING_HEADER);

        for (int index = 1; index < roomRows.size(); index++) {
            ArrayList<String> row = roomRows.get(index);
            requireColumnCount(roomsFile, index + 1, row, 3);

            try {
                String type = row.get(1);
                String typeChoice;

                if (type.equals("Standard")) {
                    typeChoice = "1";
                } else if (type.equals("Deluxe")) {
                    typeChoice = "2";
                } else {
                    throw new IllegalArgumentException(
                        "Room type must be Standard or Deluxe."
                    );
                }

                rooms.addRoom(
                    row.get(0),
                    typeChoice,
                    parsePositiveNumber(row.get(2), "pricePerNight")
                );
            } catch (RuntimeException e) {
                throw recordError(roomsFile, index + 1, e);
            }
        }

        for (int index = 1; index < guestRows.size(); index++) {
            ArrayList<String> row = guestRows.get(index);
            requireColumnCount(guestsFile, index + 1, row, 3);

            try {
                guests.addGuest(row.get(0), row.get(1), row.get(2));
            } catch (RuntimeException e) {
                throw recordError(guestsFile, index + 1, e);
            }
        }

        for (int index = 1; index < bookingRows.size(); index++) {
            ArrayList<String> row = bookingRows.get(index);
            requireColumnCount(bookingsFile, index + 1, row, 7);

            try {
                bookings.restoreBooking(
                    row.get(0),
                    row.get(1),
                    row.get(2),
                    parseDate(row.get(3), "checkIn"),
                    parseDate(row.get(4), "checkOut"),
                    parsePositiveNumber(row.get(5), "pricePerNight"),
                    parseBoolean(row.get(6))
                );
            } catch (RuntimeException e) {
                throw recordError(bookingsFile, index + 1, e);
            }
        }

        return loadedData;
    }

    private static ArrayList<ArrayList<String>> readAndValidateHeader(
            Path file, List<String> expectedHeader) throws IOException {

        ArrayList<ArrayList<String>> rows;

        try {
            rows = CsvUtil.read(file);
        } catch (IOException e) {
            throw new IOException(
                file.getFileName() + ": " + e.getMessage(), e
            );
        }

        if (rows.isEmpty()) {
            throw new IOException(
                file.getFileName() + " is empty and has no header."
            );
        }

        if (!rows.get(0).equals(expectedHeader)) {
            throw new IOException(
                file.getFileName() + " has an invalid header. Expected: "
                + String.join(",", expectedHeader)
            );
        }

        return rows;
    }

    private static void requireColumnCount(
            Path file,
            int recordNumber,
            List<String> row,
            int expectedCount) throws IOException {

        if (row.size() != expectedCount) {
            throw new IOException(
                file.getFileName() + " record " + recordNumber
                + " has " + row.size() + " columns; expected "
                + expectedCount + "."
            );
        }
    }

    private static double parsePositiveNumber(
            String value, String columnName) {

        double number;

        try {
            number = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                columnName + " is not a valid number."
            );
        }

        if (!Double.isFinite(number) || number <= 0) {
            throw new IllegalArgumentException(
                columnName + " must be a positive, finite number."
            );
        }

        return number;
    }

    private static LocalDate parseDate(
            String value, String columnName) {

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                columnName + " must use YYYY-MM-DD."
            );
        }
    }

    private static boolean parseBoolean(String value) {
        if (value.equals("true")) {
            return true;
        }

        if (value.equals("false")) {
            return false;
        }

        throw new IllegalArgumentException(
            "cancelled must be exactly true or false."
        );
    }

    private static IOException recordError(
            Path file, int recordNumber, RuntimeException cause) {

        return new IOException(
            file.getFileName() + " record " + recordNumber + ": "
            + cause.getMessage(),
            cause
        );
    }

    private static ArrayList<List<String>> roomRecords(
            HotelData hotelData) {

        ArrayList<List<String>> records = new ArrayList<>();
        records.add(ROOM_HEADER);

        for (Room room : hotelData.getRoomService().getRooms()) {
            records.add(List.of(
                room.getRoomNumber(),
                room.getRoomType(),
                Double.toString(room.getPricePerNight())
            ));
        }

        return records;
    }

    private static ArrayList<List<String>> guestRecords(
            HotelData hotelData) {

        ArrayList<List<String>> records = new ArrayList<>();
        records.add(GUEST_HEADER);

        for (Guest guest : hotelData.getGuestService().getGuests()) {
            records.add(List.of(
                guest.getGuestId(), guest.getName(), guest.getPhone()
            ));
        }

        return records;
    }

    private static ArrayList<List<String>> bookingRecords(
            HotelData hotelData) {

        ArrayList<List<String>> records = new ArrayList<>();
        records.add(BOOKING_HEADER);

        for (Booking booking
                : hotelData.getBookingService().getBookings()) {
            records.add(List.of(
                booking.getBookingId(),
                booking.getGuest().getGuestId(),
                booking.getRoom().getRoomNumber(),
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString(),
                Double.toString(booking.getPricePerNight()),
                Boolean.toString(booking.isCancelled())
            ));
        }

        return records;
    }

}
