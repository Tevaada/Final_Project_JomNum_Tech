package model;

public class Guest {
    private final String guestId;
    private String name;
    private String phone;

    public Guest(String guestId, String name, String phone) {
        if (guestId == null || guestId.isBlank()) {
            throw new IllegalArgumentException(
                "Guest ID cannot be empty."
            );
        }

        this.guestId = guestId.trim();
        updateDetails(name, phone);
    }

    public String getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public final void updateDetails(String name, String phone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Guest name cannot be empty."
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException(
                "Phone number cannot be empty."
            );
        }

        String cleanedPhone = phone.trim();

        if (!cleanedPhone.matches("\\+?[0-9]{8,15}")) {
            throw new IllegalArgumentException(
                "Phone must contain 8-15 digits, optionally starting with +."
            );
        }

        // Change the fields only after both values pass validation.
        this.name = name.trim();
        this.phone = cleanedPhone;
    }

    public void displayInfo() {
        System.out.println("------------------------------");
        System.out.println("Guest ID: " + guestId);
        System.out.println("Name: " + name);
        System.out.println("Phone: " + phone);
    }
}
