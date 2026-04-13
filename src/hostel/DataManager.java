package hostel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static final String FILE_PATH = "hostel_data.dat";

    @SuppressWarnings("unchecked")
    public static List<Student> loadStudents() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                return (List<Student>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load data: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public static void saveStudents(List<Student> students) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(new ArrayList<>(students));
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }

    public static void clearData() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}
