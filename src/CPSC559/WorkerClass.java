package CPSC559;

import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;
import java.net.ConnectException;

public class WorkerClass {
    public static String host;
    public static int port;
    public static UserDB UDB;
    public static BookDB BDB;
    //todo
    public static Book searchBook(String bookTitle) throws IOException {
        System.out.println("we are searching books");
        return BDB.getBook(bookTitle, 1);
    }

    public static User searchUser(String userID) throws IOException {
        System.out.println("we are searching users");
        return UDB.getUser(userID, 2);
    }

    public static boolean borrow(String userID, String bookID) throws IOException {
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

    public static void returnBook(String bookID) throws IOException {
        System.out.println("we are returning book");
        Book b = BDB.getBook(bookID, 0);
        b.holder = -1;
        BDB.updateBook(b);
    }

    public static void modifyFees(String userID, String feeDifference) throws IOException { 
        System.out.println("we are changing fees");
        User u = UDB.getUser(userID, 0);
        u.fines += Double.parseDouble(feeDifference);
        UDB.updateUser(u);
    }

    public static void kill(String option) {
        System.out.println("we are killing");
    }


    public static void main(String[]args) throws IOException {
        //TODO open port and listen for leader process
        //TODO execute leader process on the dB
        //TODO notify other proceses of db change/acknowledges
        System.out.println("hello java welcome back");
        //public Socket(InetAddress address,
        //      int port)
        try{
            if (args.length < 4) {
                System.err.println("Arguments must be in the form <Host> <Port> <User DB full file path> <Book DB full file path>");
            }
            host = args[0];
            port = Integer.parseInt(args[1]);
            UDB = new UserDB(args[2]);
            BDB = new BookDB(args[3]);

            Socket connect = new Socket(host,port);
            InputStream input = connect.getInputStream();
            char nextChar = (char) input.read();
            String command = "";
            while(nextChar != '\0') {
                System.out.print( nextChar );
                nextChar = (char) input.read();
                command+= nextChar;
                if(nextChar != '\n'){
                    //detected the end of a command
                    switch(command.split("_")[0]) {
                        case "s":
                            // Command format s_BookTitle
                            searchBook(command.split("_")[1]);
                            break;
                        case "u":
                            // Command format u_userLastName
                            searchUser(command.split("_")[1]);
                        case "b":
                            // Command format b_userID_bookID
                            borrow(command.split("_")[1],command.split("_")[2]);
                            break;
                        case "r":
                            // Command format r_bookID
                            returnBook(command.split("_")[1]);
                            break;
                        case "f":
                            // Command format f_userID_feeChangeAmount
                            modifyFees(command.split("_")[1], command.split("_")[2]);
                            break;
                        case "k":
                            kill(command.split("_")[1]);
                            break;
                    }
                }
            }

            input.close();
        } catch (ConnectException ce){
            System.out.println("Can not establish the connection");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
