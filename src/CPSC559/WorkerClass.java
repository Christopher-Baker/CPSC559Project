package CPSC559;

import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;
import java.net.ConnectException;

public class WorkerClass {
    //todo
    public static void search(string user, string option){
        system.out.print("we are searching");
    }
    public static void borrow(string user, string option){
        system.out.print("we are borrowing");
    }
    public static void returnBook(string user, string option){
        system.out.print("we are returning book");
    }
    public static void payFees(string user, string option){
        system.out.print("we are paying fees");
    }
    public static void kill(string option){
        system.out.print("we are killing");
    }


    public static void main(String[]args) throws IOException {
        //TODO open port and listen for leader process
        //TODO execute leader process on the dB
        //TODO notify other proceses of db change/acknowledges
        System.out.println("hello java welcome back");
        //public Socket(InetAddress address,
        //      int port)
        try{
            Socket connect = new Socket("localhost",9000);
            InputStream input = connect.getInputStream();
            char nextChar = (char) input.read();
            string command="";
            while(nextChar != '\0') {
                System.out.print( nextChar );
                nextChar = (char) input.read();
                command+= nextChar;
                if(nextChar != '\n'){
                    //detected the end of a command
                    if(command.split("_")[0]=="s"){
                        search(command.split("_")[1],command.split("_")[2]);
                    }
                    if(command.split("_")[0]=="b"){
                        borrow(command.split("_")[1],command.split("_")[2]);
                    }
                    if(command.split("_")[0]=="r"){
                        returnBook(command.split("_")[1],command.split("_")[2]);
                    }
                    if(command.split("_")[0]=="p"){
                        payFees(command.split("_")[2]);
                    }
                    if(command.split("_")[0]=="k"){
                        kill(command.split("_")[1]);
                    }
                }
            }

            input.close();
        }catch (ConnectException ce){
            System.out.println("Can not establish the connection");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
