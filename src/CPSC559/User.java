package CPSC559;

public class User {
    public int id;
    public String fName;
    public String lName;
    public double fines;

    public User(String fname, String lname) {
        this.fName = fname;
        this.lName = lname;
        this.fines = 0.0;
    }
}