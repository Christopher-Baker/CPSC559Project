package CPSC559;

import java.io.BufferedWriter;
import java.io.FileWriter;  
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

public class UserDB {
    public String filepath;

    public UserDB(String path) {
        this.filepath = path;
    }

    public synchronized void addUser(User user) throws IOException{
        user.id = this.readHighestId() + 1;
        String toAdd = String.join(",", Integer.toString(user.id), user.fName, user.lName, Double.toString(user.fines));
        toAdd += "\n";
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.filepath, true));
        writer.write(toAdd);
        writer.close();
    }

    ///
    // Return the first user whose data in the specified column matches the specified key
    ///
    public User getUser(String key, int col) throws IOException {
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (String.valueOf(items[col]).equals(key)) {
                User user = new User(items[1], items[2]);
                user.id = Integer.parseInt(items[0]);
                user.fines = Double.parseDouble(items[3]);
                return user;
            }
        }
        return null;
    }

    ///
    // Updates a user, u, based off the id
    // The id should never be manually changed so this shouldn't be a problem
    // The user u should have no null fields
    //
    public synchronized void updateBook(User u) throws IOException {
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0 ; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (u.id == Integer.parseInt(items[0])) {
                String toUpdate = String.join(",", Integer.toString(u.id), u.fName, u.lName, Double.toString(u.fines));
                data.set(i, toUpdate);
                break;
            }
        }
        Files.write(Paths.get(this.filepath), data, StandardCharsets.UTF_8);
    }

    // TODO implement a delete function if necessary

    private int readHighestId() throws IOException {
        int maxId = -1;
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (Integer.parseInt(items[0]) > maxId) {
                maxId = Integer.parseInt(items[0]);
            }
        }
        return maxId;
    }

}
