package hostel;

import java.io.Serializable;

public class Student implements Serializable {

    private static final long serialVersionUID = 2L;

    public enum Block { BLOCK_A, BLOCK_B, BLOCK_C, BLOCK_D }
    public enum Status { PENDING, PRESENT, ABSENT, ON_LEAVE }

    private static int idCounter = 1;

    private int id;
    private String name;
    private String rollNumber;
    private String roomNumber;
    private Block block;
    private Status status;
    private String notes;

    public Student(String name, String rollNumber, String roomNumber, Block block) {
        this.id = idCounter++;
        this.name = name;
        this.rollNumber = rollNumber;
        this.roomNumber = roomNumber;
        this.block = block;
        this.status = Status.PENDING;
        this.notes = "";
    }

    public static void resetCounter() { idCounter = 1; }
    public static void setCounter(int val) { idCounter = val; }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }
    public String getRoomNumber() { return roomNumber; }
    public Block getBlock() { return block; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }

    // Setters
    public void setStatus(Status status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getBlockString() {
        switch (block) {
            case BLOCK_A: return "Block A";
            case BLOCK_B: return "Block B";
            case BLOCK_C: return "Block C";
            case BLOCK_D: return "Block D";
            default: return "Unknown";
        }
    }

    public String getStatusString() {
        switch (status) {
            case PENDING: return "Pending";
            case PRESENT: return "Present";
            case ABSENT: return "Absent";
            case ON_LEAVE: return "On Leave";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "S#" + id + " - " + name + " (" + getBlockString() + " - " + roomNumber + ")";
    }
}
