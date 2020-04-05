package CPSC559;

import java.lang.Thread;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Math;

public class WorkerThread extends Thread {
    protected UserDB UDB;
    protected BookDB BDB;
    protected PrintWriter output;
    protected BufferedReader input;
    protected ArrayList<Integer> siblings;

    WorkerThread(UserDB u, BookDB b, PrintWriter output, BufferedReader input, ArrayList<Integer> siblings) {
        this.UDB = u;
        this.BDB = b;
        this.output = output;
        this.input = input;
        this.siblings = siblings;
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

    public void forwardRequest(String request) {
        for(int i = 0; i < siblings.size(); i++) {
            try {
                Socket connect = new Socket("localhost", siblings.get(i));
                PrintWriter forwarder = new PrintWriter(connect.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                connect.setSoTimeout(30000); // So the thread doesn't hang waiting for a response from other server, this will go 10 seconds max
                forwarder.println(request);
                forwarder.flush();
                String line = reader.readLine();

                String report = line.split("%")[0];
				String command = (line.split("%")[0]).split(";")[0];
                if(command.equals(request)) {
                    if(!report.equals("ack")) {
                        connect.close();
                        throw new Exception("Server on port " + siblings.get(i) + " failed to acknowledge " + command);
                    }
                    else {
                        System.out.println("Server on port" + siblings.get(i) + "acknowledged " + command);
                    }
                }
                else {
                    //@nick plz
                }
                forwarder.close();
                reader.close();
                connect.close();
            } catch (Exception e) {
                System.err.println("Error occured while trying to forward request to server on port " + siblings.get(i) +"\nMessage: " + e.getMessage());
            }
        }
    }

    private void processReq(String command, boolean forward) throws IOException {
        switch(command.split("_")[0]) {
            case "s":
                // Command format: s_BookTitle
                Book b = searchBook(command.split("_")[1]);
                if (b == null) {
                    String retMsg = "nack%" + command + ';';
                    output.println(retMsg);
                } else {
                    String retMsg = "ack%" + command + ';' + b.toString();
                    output.println(retMsg);
                }
                System.out.println("Printing book:" + b.toString());
                output.flush();
                break;
            case "u":
                // Command format: u_userLastName
                User u = searchUser(command.split("_")[1]);
                if (u == null) {
                    String retMsg = "nack%" + command + ';';
                    output.println(retMsg);
                } else {
                     String retMsg = "ack%" + command + ';' + u.toString();
                    output.println(retMsg);
                }
                System.out.println("Printing user:" + u.toString());
                output.flush();
                break;
            case "b":
                // Command format: b_userID_bookID
                boolean success = borrow(command.split("_")[1],command.split("_")[2]);
                if (success) {
                    String retMsg = "ack%" + command + ';';
                    output.println(retMsg);
                    if (forward) {
                        forwardRequest("i_" + command);
                    }
                } else {
                    String retMsg = "nack%" + command + ';';
                    output.println(retMsg);
                }
                output.flush();
                break;
            case "r":
                // Command format: r_bookID
                returnBook(command.split("_")[1]);
                String retMsg = "ack%" + command + ';';
                output.println(retMsg);
                output.flush();
                if (forward) {
                    forwardRequest("i_" + command);
                }
                break;
            case "f":
                // Command format: f_userID_feeChangeAmount
                modifyFees(command.split("_")[1], command.split("_")[2]);
                String retMsg = "ack%" + command + ';';
                output.println(retMsg);
                output.flush();
                if (forward) {
                    forwardRequest("i_" + command);
                }
                break;
            case "h":
                // Command format: h
                // Heartbeat
                output.println("ack");
                output.flush();
                break;
            case "l":
                // Command format: l
                // get load
                OperatingSystemMXBean osmxb = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                int load = Math.toIntExact(Math.round(osmxb.getProcessCpuLoad()));
                output.println(load);
                output.flush();
                break;
            case "i":
                //internal forwarded message
                String[] parts = command.split("_");
                processReq(String.join("_", Arrays.copyOfRange(parts, 1, parts.length)), false); // Do not forward forwarded request
                break;
        }
    }

    public void run() {
        try {
            String command = input.readLine();

            System.out.println("Processing command: " + command);

            processReq(command, true);

        } catch (Exception e) {
            System.err.println("Encountered Exception: " + e.getMessage());
        }
    }
}