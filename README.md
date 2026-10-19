# Hotel Booking System

A Java console application for managing rooms, guests, and hotel
bookings. The system applies booking rules, calculates stay costs, and
stores its data in human-readable CSV files.

[View the GitHub repository](https://github.com/Tevaada/Final_Project_JomNum_Tech)

## Overview

The application provides a menu-driven interface for common hotel
booking tasks. It supports room and guest management, booking conflict
detection, cancellations, booking-history removal, and persistent CSV
storage.

The project is written for JDK 17 and uses only the Java standard
library.

## Features

### Room Management

- Add Standard and Deluxe rooms
- View room details and nightly prices
- Update room prices
- Delete rooms that are not referenced by a booking
- Reject duplicate room numbers and invalid prices

### Guest Management

- Register and view guests
- Update guest names and phone numbers
- Delete guests that are not referenced by a booking
- Reject duplicate guest IDs and invalid phone numbers

### Booking Management

- Create bookings for registered guests and existing rooms
- View confirmed and cancelled bookings
- Prevent overlapping confirmed bookings for the same room
- Allow a new stay to begin on another booking's checkout date
- Calculate the number of nights and total cost
- Preserve the nightly price agreed when the booking was created
- Cancel bookings while keeping their history
- Permanently delete cancelled booking history from option 4
- Reuse dates released by a cancelled booking

## Project Structure

```text
Final_Assigment/
├── data/
│   ├── rooms.csv
│   ├── guests.csv
│   └── bookings.csv
├── src/
│   ├── exception/
│   │   └── BookingException.java
│   ├── model/
│   │   ├── Booking.java
│   │   ├── DeluxeRoom.java
│   │   ├── Guest.java
│   │   ├── HotelData.java
│   │   ├── Room.java
│   │   └── StandardRoom.java
│   ├── repository/
│   │   └── Repository.java
│   ├── service/
│   │   ├── BookingService.java
│   │   ├── GuestService.java
│   │   └── RoomService.java
│   ├── util/
│   │   ├── CsvUtil.java
│   │   └── FileManager.java
│   └── Main.java
├── .gitignore
└── README.md
```

### Package Responsibilities

| Package | Responsibility |
| --- | --- |
| `model` | Represents rooms, guests, bookings, and application data |
| `service` | Contains room, guest, and booking business logic |
| `repository` | Provides reusable generic storage with `Repository<T>` |
| `exception` | Defines booking-specific errors |
| `util` | Parses CSV and loads or saves application data |
| `Main` | Handles menus, user input, and application flow |

## Requirements

- JDK 17 or newer
- A terminal or Java IDE

Confirm that Java is installed:

```bash
java -version
javac -version
```

## Build and Run

Open a terminal in the project root—the directory containing `src`
and `data`.

Compile the application:

```bash
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/Main.java
```

Run the application:

```bash
java -cp out Main
```

Run the program from the same project root each time so it uses the
correct `data` directory.

## Application Menu

```text
1. Manage Rooms
2. Manage Guests
3. Create Booking
4. View Bookings
5. Cancel Booking
0. Save and Exit
```

Option 4 displays the complete booking history and provides this
submenu:

```text
1. Delete Cancelled Booking
0. Back to Main Menu
```

A booking must first be cancelled through option 5. Permanent history
deletion then requires the booking ID and the confirmation word
`DELETE`.

## Typical Booking Workflow

1. Register a guest through **Manage Guests**.
2. Add a room or select an existing room.
3. Choose **Create Booking**.
4. Enter a unique booking ID, guest ID, and room number.
5. Enter the check-in and checkout dates in `YYYY-MM-DD` format.
6. View the booking through option 4.

Example calculation:

```text
Nightly price: $40.00
Check-in:     2026-11-20
Check-out:    2026-11-23
Nights:       3
Total:        $120.00
```

## Business Rules

- Room numbers, guest IDs, and booking IDs must be unique.
- A booking requires an existing room and guest.
- Checkout must be after check-in.
- Confirmed bookings for the same room cannot overlap.
- Checkout and the next check-in may occur on the same date.
- Cancelled bookings do not block room availability.
- Only cancelled bookings can be permanently deleted.
- Each booking retains its original nightly price.
- A referenced room or guest cannot be deleted.
- After all related booking history is permanently deleted, the room
  or guest may also be deleted.

The system accepts past booking dates. It validates the date order and
room availability but does not restrict check-in to the current or a
future date.

## CSV Persistence

Data is stored in three UTF-8 CSV files.

| File | Header |
| --- | --- |
| `data/rooms.csv` | `roomNumber,type,pricePerNight` |
| `data/guests.csv` | `guestId,name,phone` |
| `data/bookings.csv` | `bookingId,guestId,roomNumber,checkIn,checkOut,pricePerNight,cancelled` |

IDs and phone numbers are stored as strings, which preserves leading
zeros. Dates use ISO format (`YYYY-MM-DD`), and prices use a
locale-independent number format.

The CSV parser correctly handles commas, quotation marks, empty
fields, and quoted multiline values. During loading, the application:

1. Validates each CSV header and record.
2. Loads rooms and guests before bookings.
3. Reconnects each booking to its matching room and guest.
4. Rejects duplicate IDs, missing references, invalid values, and
   overlapping confirmed bookings.

If none of the CSV files exists, the application creates all three and
adds sample rooms 101, 102, and 201. If only some files exist, startup
stops instead of creating a mixed dataset.

The application saves after each successful change and when **Save and
Exit** is selected. If writing fails, it displays an error and does not
report a successful save.

## Java Concepts Demonstrated

- Classes and objects
- Encapsulation and input validation
- Inheritance with `StandardRoom` and `DeluxeRoom`
- Abstraction through the abstract `Room` class
- Polymorphism through `Room` references
- Generics with `Repository<T>`
- `ArrayList` and `HashMap` collections
- Custom exceptions and exception handling
- `LocalDate` and date calculations
- UTF-8 file I/O and try-with-resources
- Arrow-style switch expressions
