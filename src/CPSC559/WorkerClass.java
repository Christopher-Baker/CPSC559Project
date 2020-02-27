package CPSC559;
import java.io.IOException;
import java.lang.Object;
import java.net.Socket;

public class WorkerClass {
    public static void main(String[]args) throws IOException {
        //TODO open port and listen for leader process
        //TODO execute leader process on the dB
        //TODO notify other proceses of db change/acknowledges
        System.out.println("hello java welcome back");
        //public Socket(InetAddress address,
        //      int port)
        Socket connect = new Socket("localhost",9000);

    }
}
