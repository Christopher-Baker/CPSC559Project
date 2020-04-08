package CPSC559;

import java.io.*;
import java.lang.Thread;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import java.util.Date;

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
				String command = (line.split("%")[1]).split(";")[0];
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
                	//announce send sequence
                	//send sequence
                	
                	//figure out where check for receive sequence goes
                	//Figure out where receive sequence goes
                }
                forwarder.close();
                reader.close();
                connect.close();
            } catch (Exception e) {
                System.err.println("Error occured while trying to forward request to server on port " + siblings.get(i) +"\nMessage: " + e.getMessage());
            }
        }
    }

    private void sendingSequence(String target, int targetPort) throws IOException {

        File[] databases = new File[2];
        Socket socket = new Socket(InetAddress.getByName(target), targetPort);

        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);

        databases[0] = new File(BDB.filepath);
        databases[1] = new File(UDB.filepath);
        dos.writeInt(databases.length);

        for(File db : databases){
            long length = db.length();
            dos.writeLong(length);

            String name = db.getName();
            System.out.println("Sending " + name + " to " + target + ":" + targetPort);

            dos.writeUTF(name);

            FileInputStream fis = new FileInputStream(db);
            BufferedInputStream bis = new BufferedInputStream(fis);

            int theByte = 0;
            while((theByte = bis.read()) != -1) bos.write(theByte);
            bis.close();
        }

    }

    private void receivingSequence(int listeningPort) throws IOException {

        final String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String[] fileNames = new String[2];

        ServerSocket srvSocket = new ServerSocket(listeningPort);
        Socket socket = srvSocket.accept();

        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        int filesCount = dis.readInt();
        File[] files = new File[filesCount];

        for(int i = 0; i < filesCount; i++) {
            long fileLength = dis.readLong();
            String fileName = dis.readUTF();
            
            if(fileName.contains("books")){
                fileNames[0] = fileName;
            }else if(fileName.contains("users")){
                fileNames[1] = fileName;
            }

            System.out.println("Receiving " + fileName);

            files[i] = new File(System.getProperty("user.dir") + "/" + fileName + timeStamp);

            FileOutputStream fos = new FileOutputStream(files[i]);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            for(int j = 0; j < fileLength; j++) bos.write(bis.read());

            bos.close();
        }

        dis.close();

        WorkerClass.setBDB(System.getProperty("user.dir") + "/" + fileNames[0] + timeStamp);
        WorkerClass.setUDB(System.getProperty("user.dir") + "/" + fileNames[1] + timeStamp);

    }


    private void processReq(String command, boolean forward) throws IOException {
        String retMsg = "";
        switch(command.split("_")[0]) {
            case "s":
                // Command format: s_BookTitle
                Book b = searchBook(command.split("_")[1]);
                if (b == null) {
                    retMsg = "nack%" + command + ';';
                    output.println(retMsg);
                } else {
                    retMsg = "ack%" + command + ';' + b.toString();
                    output.println(retMsg);
                }
                System.out.println("Printing book:" + b.toString());
                output.flush();
                break;
            case "u":
                // Command format: u_userLastName
                User u = searchUser(command.split("_")[1]);
                if (u == null) {
                    retMsg = "nack%" + command + ';';
                    output.println(retMsg);
                } else {
                    retMsg = "ack%" + command + ';' + u.toString();
                    output.println(retMsg);
                }
                System.out.println("Printing user:" + u.toString());
                output.flush();
                break;
            case "b":
                // Command format: b_userID_bookID
                boolean success = borrow(command.split("_")[1],command.split("_")[2]);
                if (success) {
                    retMsg = "ack%" + command + ';';
                    output.println(retMsg);
                    if (forward) {
                        forwardRequest("i_" + command);
                    }
                } else {
                    retMsg = "nack%" + command + ';';
                    output.println(retMsg);
                }
                output.flush();
                break;
            case "r":
                // Command format: r_bookID
                returnBook(command.split("_")[1]);
                retMsg = "ack%" + command + ';';
                output.println(retMsg);
                output.flush();
                if (forward) {
                    forwardRequest("i_" + command);
                }
                break;
            case "f":
                // Command format: f_userID_feeChangeAmount
                modifyFees(command.split("_")[1], command.split("_")[2]);
                retMsg = "ack%" + command + ';';
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
            case "sendDB":
                // send the database to target
                // message: sendDB_TargetAddr:TargetPort
                String[] dest = command.split("_")[1].split(":");
                sendingSequence(dest[0], Integer.parseInt(dest[1]));
                break;

            case "recvDB":
                // receive the database from primary
                // message: recvDB_ListeningPort
                String port = command.split("_")[1];
                receivingSequence(Integer.parseInt(port));
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