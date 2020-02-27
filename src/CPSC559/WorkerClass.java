package CPSC559;
import java.io.IOException;
import java.lang.Object;
import java.net.Socket;
import java.io.InputStream;
import java.util.Arrays;

public class WorkerClass {
    public static void main(String[]args) throws IOException {
        //TODO open port and listen for leader process
        //TODO execute leader process on the dB
        //TODO notify other proceses of db change/acknowledges
        System.out.println("hello java welcome back");
        //public Socket(InetAddress address,
        //      int port)
        Socket connect = new Socket("localhost",9000);
        
        InputStream input = connect.getInputStream();
        
        char nextChar = (char) input.read();
        
        while(nextChar != '\0') {
        	System.out.print( nextChar );
        	nextChar = (char) input.read();
        }
        
        input.close();

    }
}
