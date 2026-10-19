package service;

import model.Guest;
import repository.Repository;

import java.util.ArrayList;
import java.util.Locale;

public class GuestService {
    private final Repository<Guest> guestRepository =
        new Repository<>();

    private String normalizeId(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            throw new IllegalArgumentException(
                "Guest ID cannot be empty."
            );
        }

        return guestId.trim().toUpperCase(Locale.ROOT);
    }

    public void addGuest(
            String guestId, String name, String phone) {

        String id = normalizeId(guestId);

        if (guestRepository.exists(id)) {
            throw new IllegalArgumentException(
                "Guest ID already exists."
            );
        }

        Guest guest = new Guest(id, name, phone);

        guestRepository.add(id, guest);
    }

    public void viewGuests() {
        ArrayList<Guest> guests = guestRepository.findAll();

        if (guests.isEmpty()) {
            System.out.println("No guests registered.");
            return;
        }

        for (Guest guest : guests) {
            guest.displayInfo();
        }
    }

    public Guest findGuest(String guestId) {
        String id = normalizeId(guestId);
        return guestRepository.findById(id);
    }

    public void updateGuest(
            String guestId,
            String newName,
            String newPhone) {

        Guest guest = requireGuest(guestId);
        guest.updateDetails(newName, newPhone);
    }

    public void deleteGuest(String guestId) {
        Guest guest = requireGuest(guestId);
        guestRepository.delete(guest.getGuestId());
    }

    public ArrayList<Guest> getGuests() {
        return guestRepository.findAll();
    }

    private Guest requireGuest(String guestId) {
        Guest guest = findGuest(guestId);

        if (guest == null) {
            throw new IllegalArgumentException(
                "Guest not found."
            );
        }

        return guest;
    }
}
