package model;

public class StandardRoom extends Room {
    public StandardRoom(String roomNumber, double pricePerNight){
        super(roomNumber, pricePerNight);
    }

    @Override 
    public String getRoomType(){
        return "Standard";
    }
    
    @Override
    public String getDescription(){
        return "A comfortable room with a bed, Wi-Fi, and air conditioning.";
    }
}
