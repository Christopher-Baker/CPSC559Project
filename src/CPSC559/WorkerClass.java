package CPSC559;

import java.net.Socket;
import java.io.InputStream;
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
    //todo
    


    public static void main(String[]args) throws IOException {
        //TODO open port and listen for leader process
        //TODO execute leader process on the dB
        //TODO notify other proceses of db change/acknowledges
        System.out.println("hello java welcome back");
        //public Socket(InetAddress address,
        //      int port)
        try{
            if (args.length != 3) {
                System.err.println("Arguments must be in the form <Server Port> <User DB full file path> <Book DB full file path>");
            }

            port = Integer.parseInt(args[0]); // port this server will run on
            UDB = new UserDB(args[1]);
            BDB = new BookDB(args[2]);
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
                WorkerThread wt = new WorkerThread(UDB, BDB, output, input);
                wt.start();
                // TODO figure out how to kill this loop from thread
            }

            input.close();
            output.close();
            connect.close();
        } catch (ConnectException ce){
            System.out.println("Can not establish the connection");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
