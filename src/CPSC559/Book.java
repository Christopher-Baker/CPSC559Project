package CPSC559;

public class Book {
    public int id;
    public String title;
    public int holder; // The user id of the user who has the book, -1 if it's available

    public Book(String title) {
        this.title = title;
        this.holder = -1;
    }
}