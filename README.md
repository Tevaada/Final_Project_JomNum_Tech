# Hotel Booking System

**Project Name** HOTEL BOOKING SYSTEM  
**Course:** Java Programming

**Repository:** [Final Project JomNum Tech](https://github.com/Tevaada/Final_Project_JomNum_Tech)

## About the Project

This is a Java console application for managing a small hotel. It can
manage rooms and guests, create bookings, prevent booking conflicts,
calculate booking totals, and save data in CSV files.

The project uses only the Java standard library and is compatible with
JDK 17.

## Main Features

### Rooms

- Add Standard and Deluxe rooms.
- View room information and prices.
- Update a room's nightly price.
- Delete a room if it has never been used in a booking.
- Reject duplicate room numbers and invalid prices.

### Guests

- Add and view guests.
- Update guest names and phone numbers.
- Delete a guest if the guest has no booking history.
- Reject duplicate guest IDs and invalid phone numbers.

### Bookings

- Create a booking for an existing guest and room.
- View confirmed and cancelled bookings.
- Cancel a booking without deleting its history.
- Calculate the number of nights and total price.
- Prevent two confirmed bookings from using the same room on
  overlapping dates.
- Allow a new booking to begin on another booking's checkout date.
- Allow cancelled dates to be booked again.
- Keep the original booked price if the room price changes later.

## Project Structure

```text
Final_Assigment/
├── data/
│   ├── rooms.csv
│   ├── guests.csv
│   ├── bookings.csv
│   └── *.csv.bak
├── src/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── util/
│   └── Main.java
└── README.md
```

| Location | Purpose |
| --- | --- |
| `src/Main.java` | Menus, input, and application flow |
| `src/model/` | Room, guest, booking, and hotel data classes |
| `src/service/` | Room, guest, and booking logic |
| `src/repository/` | Generic `Repository<T>` storage |
| `src/exception/` | Custom booking exception |
| `src/util/` | CSV parsing, loading, saving, and recovery |
| `data/` | Active data and recovery backups |

## Requirements

- JDK 17 or newer
- A terminal or an IDE such as VS Code

Check that Java is installed:

```bash
java -version
javac -version
```

## Compile and Run

Open a terminal in the project root, where the `src` and `data`
folders are located.

Compile:

```bash
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/Main.java
```

Run:

```bash
java -cp out Main
```

Always run the application from the project root so it uses the
correct `data` folder.

## Menu

```text
1. Manage Rooms
2. Manage Guests
3. Create Booking
4. View Bookings
5. Cancel Booking
0. Save and Exit
```

To create a booking:

1. Add a guest from **Manage Guests**.
2. Add a room or use one of the existing rooms.
3. Choose **Create Booking**.
4. Enter a unique booking ID, guest ID, and room number.
5. Enter dates using `YYYY-MM-DD`.

Example:

```text
Room price: $40.00 per night
Check-in:  2026-11-20
Check-out: 2026-11-23
Nights:    3
Total:     $120.00
```

## Booking Rules

- Booking IDs, guest IDs, and room numbers must be unique.
- A booking requires an existing guest and room.
- Checkout must be after check-in.
- Confirmed bookings for the same room cannot overlap.
- Checkout and the next check-in may be on the same date.
- Cancelled bookings do not block the room.
- A booking keeps the nightly price agreed when it was created.
- Rooms and guests referenced by booking history cannot be deleted.

The application does not prevent bookings with past dates. It only
checks that checkout is after check-in and that the room is available.

## CSV Data

The application stores data in three UTF-8 CSV files.

### `data/rooms.csv`

```text
roomNumber,type,pricePerNight
```

### `data/guests.csv`

```text
guestId,name,phone
```

### `data/bookings.csv`

```text
bookingId,guestId,roomNumber,checkIn,checkOut,pricePerNight,cancelled
```

IDs and phone numbers are stored as text, so leading zeros are kept.
Dates use the ISO `YYYY-MM-DD` format. Numbers are stored in a
locale-independent format.

The CSV reader supports commas, quotation marks, empty fields, and
quoted multiline fields. It validates headers and records instead of
silently ignoring damaged data.

Rooms and guests are loaded before bookings. Each loaded booking is
reconnected to its matching guest and room.

## Saving and Recovery

The application saves after every successful change and again when
**Save and Exit** is selected.

During a save, it:

1. Writes all three datasets to temporary files.
2. Reads the temporary files back to validate them.
3. Creates `.csv.bak` backups of the current files.
4. Uses a `.save-pending` marker while replacing the files.
5. Restores the previous complete dataset if replacement fails.

Replacing three files is not one atomic operation, so the backups and
recovery marker prevent a partially saved dataset from being accepted.

Do not delete `.csv.bak` or `.save-pending` files manually. Do not run
multiple copies of the application against the same data folder.

## Java Concepts Demonstrated

- Classes and objects
- Encapsulation and validation
- Inheritance through `StandardRoom` and `DeluxeRoom`
- Abstraction through the abstract `Room` class
- Polymorphism through `Room` references
- Generics through `Repository<T>`
- `ArrayList` and `HashMap` collections
- Custom exceptions and error handling
- `LocalDate` and date calculations
- File I/O and try-with-resources
- Modern arrow-style switch statements
