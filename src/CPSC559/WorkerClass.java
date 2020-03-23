package CPSC559;

import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ConnectException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.io.PrintWriter;

public class WorkerClass {
    public static String host;
    public static int port;
    public static UserDB UDB;
    public static BookDB BDB;
    public static ArrayList<Integer> siblings;    
    public static void main(String[]args) throws IOException {
        try{
            if (args.length != 3) {
                System.err.println("Arguments must be in the form <Server Port> <User DB full file path> <Book DB full file path> <sibling servers by port seperated by comma>");
                System.err.println("For example: java CPSC559.WorkerClass 9001 /home/userDB.csv /home/bookDB.csv 9002,9003,9004");
            }

            port = Integer.parseInt(args[0]); // port this server will run on
            UDB = new UserDB(args[1]);
            BDB = new BookDB(args[2]);
            String[] string_siblings = args[3].split(",");
            siblings = new ArrayList<Integer>();
            for (int i = 0; i < string_siblings.length; i++) {
                siblings.add(Integer.parseInt(string_siblings[i]));
            }
            Socket connect = null;
            BufferedReader input = null;
            PrintWriter output = null;
            ServerSocket ss = new ServerSocket(port);
            boolean kill = false;

            while (!kill) {
                connect = ss.accept();
                input = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                output = new PrintWriter(connect.getOutputStream());
                // right now there is no thread pool, 
                // The threads are responsible for killing themselves and not crashing the system by running forever
                WorkerThread wt = new WorkerThread(UDB, BDB, output, input, siblings);
                wt.start();
                // TODO figure out how to kill this loop from thread
                    // This may be impossible, we may just have to ctrl-c to kill 
            }

            input.close();
            output.close();
            connect.close();
            ss.close();
        } catch (ConnectException ce){
            System.out.println("Can not establish the connection");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
