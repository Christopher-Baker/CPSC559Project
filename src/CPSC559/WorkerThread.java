package CPSC559;

import java.lang.Thread;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;

public class WorkerThread extends Thread {
    protected UserDB UDB;
    protected BookDB BDB;
    protected PrintWriter output;
    protected BufferedReader input;

    WorkerThread(UserDB u, BookDB b, PrintWriter output, BufferedReader input) {
        this.UDB = u;
        this.BDB = b;
        this.output = output;
        this.input = input;
    }

    public Book searchBook(String bookTitle) throws IOException {
        System.out.println("we are searching books");
        return BDB.getBook(bookTitle, 1);
    }

    public User searchUser(String userID) throws IOException {
        System.out.println("we are searching users");
        return UDB.getUser(userID, 2);
    }

    public boolean borrow(String userID, String bookID) throws IOException {
        System.out.println("we are borrowing");
        Book b = BDB.getBook(bookID, 0);
        if (b.holder != -1) {
            // This book is not available
            return false;
        }
        b.holder = Integer.parseInt(userID);
        BDB.updateBook(b);
        return true;
    }

    public void returnBook(String bookID) throws IOException {
        System.out.println("we are returning book");
        Book b = BDB.getBook(bookID, 0);
        b.holder = -1;
        BDB.updateBook(b);
    }

    public void modifyFees(String userID, String feeDifference) throws IOException { 
        System.out.println("we are changing fees");
        User u = UDB.getUser(userID, 0);
        u.fines += Double.parseDouble(feeDifference);
        UDB.updateUser(u);
    }

    public void run() {
        try {
            String command = input.readLine();

            System.out.println("Processing command: " + command);

            switch(command.split("_")[0]) {
                case "s":
                    // Command format: s_BookTitle
                    Book b = searchBook(command.split("_")[1]);
                    if (b == null) {
                        output.println("nack");
                    } else {
                        output.println(b.toString());
                    }
                    break;
                case "u":
                    // Command format: u_userLastName
                    User u = searchUser(command.split("_")[1]);
                    if (u == null) {
                        output.println("nack");
                    } else {
                        output.println(u.toString());
                    }
                case "b":
                    // Command format: b_userID_bookID
                    boolean success = borrow(command.split("_")[1],command.split("_")[2]);
                    if (success) {
                        output.println("ack");
                    } else {
                        output.println("nack");
                    }
                    break;
                case "r":
                    // Command format: r_bookID
                    returnBook(command.split("_")[1]);
                    output.println("ack");
                    break;
                case "f":
                    // Command format: f_userID_feeChangeAmount
                    modifyFees(command.split("_")[1], command.split("_")[2]);
                    output.println("ack");
                    break;
                case "h":
                    // Command format: h
                    // Heartbeat
                    output.println("ack");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Encountered Exception: " + e.getMessage());
        }
    }
}