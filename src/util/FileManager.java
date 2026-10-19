package util;

import model.Booking;
import model.Guest;
import model.HotelData;
import model.Room;
import service.BookingService;
import service.GuestService;
import service.RoomService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class FileManager {

    private static final Path DATA_DIRECTORY =
        Path.of("data").toAbsolutePath();
    private static final Path ROOMS_FILE =
        DATA_DIRECTORY.resolve("rooms.csv");
    private static final Path GUESTS_FILE =
        DATA_DIRECTORY.resolve("guests.csv");
    private static final Path BOOKINGS_FILE =
        DATA_DIRECTORY.resolve("bookings.csv");
    private static final Path SAVE_MARKER =
        DATA_DIRECTORY.resolve(".save-pending");

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
        recoverInterruptedSave();

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
        recoverInterruptedSave();

        Path roomsTemporary = Files.createTempFile(
            DATA_DIRECTORY, "rooms-", ".tmp"
        );
        Path guestsTemporary = Files.createTempFile(
            DATA_DIRECTORY, "guests-", ".tmp"
        );
        Path bookingsTemporary = Files.createTempFile(
            DATA_DIRECTORY, "bookings-", ".tmp"
        );

        boolean markerCreated = false;
        boolean roomsExisted = Files.exists(ROOMS_FILE);
        boolean guestsExisted = Files.exists(GUESTS_FILE);
        boolean bookingsExisted = Files.exists(BOOKINGS_FILE);

        try {
            CsvUtil.write(roomsTemporary, roomRecords(hotelData));
            CsvUtil.write(guestsTemporary, guestRecords(hotelData));
            CsvUtil.write(bookingsTemporary, bookingRecords(hotelData));

            // Read all temporary files back before replacing live data.
            loadCsvData(
                roomsTemporary, guestsTemporary, bookingsTemporary
            );

            prepareBackup(ROOMS_FILE, roomsExisted);
            prepareBackup(GUESTS_FILE, guestsExisted);
            prepareBackup(BOOKINGS_FILE, bookingsExisted);

            writeSaveMarker(
                roomsExisted, guestsExisted, bookingsExisted
            );
            markerCreated = true;

            moveReplacing(roomsTemporary, ROOMS_FILE);
            moveReplacing(guestsTemporary, GUESTS_FILE);
            moveReplacing(bookingsTemporary, BOOKINGS_FILE);

            Files.delete(SAVE_MARKER);
            markerCreated = false;
        } catch (Exception saveFailure) {
            IOException failure = toIOException(
                "Could not save a complete CSV dataset.", saveFailure
            );

            if (markerCreated || Files.exists(SAVE_MARKER)) {
                try {
                    rollback(
                        roomsExisted, guestsExisted, bookingsExisted
                    );
                    Files.deleteIfExists(SAVE_MARKER);
                    markerCreated = false;
                } catch (IOException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
            }

            throw failure;
        } finally {
            Files.deleteIfExists(roomsTemporary);
            Files.deleteIfExists(guestsTemporary);
            Files.deleteIfExists(bookingsTemporary);
        }
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

    private static void prepareBackup(Path liveFile, boolean existed)
            throws IOException {

        Path backupFile = backupFileFor(liveFile);

        if (existed) {
            if (!Files.isRegularFile(liveFile)) {
                throw new IOException(
                    liveFile.getFileName() + " is not a regular file."
                );
            }

            Files.copy(
                liveFile,
                backupFile,
                StandardCopyOption.REPLACE_EXISTING
            );
        } else {
            Files.deleteIfExists(backupFile);
        }
    }

    private static void writeSaveMarker(
            boolean roomsExisted,
            boolean guestsExisted,
            boolean bookingsExisted) throws IOException {

        Path markerTemporary = Files.createTempFile(
            DATA_DIRECTORY, "save-marker-", ".tmp"
        );

        try {
            String markerText =
                "rooms.csv=" + roomsExisted + System.lineSeparator()
                + "guests.csv=" + guestsExisted + System.lineSeparator()
                + "bookings.csv=" + bookingsExisted
                + System.lineSeparator();

            Files.writeString(
                markerTemporary,
                markerText,
                StandardCharsets.UTF_8
            );
            moveReplacing(markerTemporary, SAVE_MARKER);
        } finally {
            Files.deleteIfExists(markerTemporary);
        }
    }

    private static void recoverInterruptedSave() throws IOException {
        if (!Files.exists(SAVE_MARKER)) {
            return;
        }

        Properties state = new Properties();

        try (java.io.BufferedReader reader = Files.newBufferedReader(
                SAVE_MARKER, StandardCharsets.UTF_8)) {
            state.load(reader);
        }

        boolean roomsExisted = markerBoolean(state, "rooms.csv");
        boolean guestsExisted = markerBoolean(state, "guests.csv");
        boolean bookingsExisted = markerBoolean(state, "bookings.csv");

        rollback(roomsExisted, guestsExisted, bookingsExisted);
        Files.delete(SAVE_MARKER);
        System.out.println(
            "Recovered the previous complete dataset after an "
            + "interrupted save."
        );
    }

    private static boolean markerBoolean(
            Properties state, String key) throws IOException {

        String value = state.getProperty(key);

        if ("true".equals(value)) {
            return true;
        }

        if ("false".equals(value)) {
            return false;
        }

        throw new IOException(
            "The save recovery marker is damaged. No recovery was "
            + "attempted."
        );
    }

    private static void rollback(
            boolean roomsExisted,
            boolean guestsExisted,
            boolean bookingsExisted) throws IOException {

        restoreOne(ROOMS_FILE, roomsExisted);
        restoreOne(GUESTS_FILE, guestsExisted);
        restoreOne(BOOKINGS_FILE, bookingsExisted);
    }

    private static void restoreOne(Path liveFile, boolean existed)
            throws IOException {

        if (existed) {
            Path backupFile = backupFileFor(liveFile);

            if (!Files.isRegularFile(backupFile)) {
                throw new IOException(
                    "Cannot recover " + liveFile.getFileName()
                    + " because its backup is missing."
                );
            }

            Files.copy(
                backupFile,
                liveFile,
                StandardCopyOption.REPLACE_EXISTING
            );
        } else {
            Files.deleteIfExists(liveFile);
        }
    }

    private static Path backupFileFor(Path liveFile) {
        return liveFile.resolveSibling(
            liveFile.getFileName().toString() + ".bak"
        );
    }

    private static void moveReplacing(Path source, Path destination)
            throws IOException {

        try {
            Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static IOException toIOException(
            String message, Exception cause) {

        return new IOException(message + " " + cause.getMessage(), cause);
    }
}
