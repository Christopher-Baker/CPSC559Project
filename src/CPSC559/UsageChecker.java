import java.util.List;
import java.util.ArrayList;
import java.net.ResponseCache;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;

import CPSC559.SocketUsagePair;

public class UsageChecker implements Runnable {

	public static List<SocketUsagePair> socketUsage = new ArrayList<SocketUsagePair>();
	private static boolean initialized = false;
	
	private final int portNum;
	private final int id;
	private Socket dbSocket = null;
	private PrintWriter toDB = null;
	private BufferedReader fromDB = null;
	private boolean connectionGood = false;
	
	public UsageChecker(int id) {
		init();
		this.id = id;
		this.portNum = socketUsage.get(id).portNum();
	}
	
	@Override
	public void run() {
		
		while(!connectionGood) {
			try{
				dbSocket = new Socket("localhost", this.portNum);
				toDB = new PrintWriter(dbSocket.getOutputStream());
				fromDB = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));
				connectionGood = true;
			}
			catch (Exception e) {
				System.out.println("Waiting on port " + this.portNum);
				connectionGood = false;
			}
		}
		
		//TODO frequency to update the usage
		this.updateUsage();
		

	}
	
	private void updateUsage() {
		this.toDB.print("usage Request Message...\n"); //TODO change to usage request
		this.toDB.flush();
		String response;
		try {
			response = this.fromDB.readLine();
			while(response == null) {
				response = this.fromDB.readLine();
				if(false /*timeout condition */) { //TODO set the timeout condition
					throw new Exception("Server timeout!");
				}
			}
			
			socketUsage.get(this.id).setUsage(Integer.parseInt(response));
			
		} catch (Exception e) {
			System.out.println("Unable to read usage response from server.");
			System.err.println(e.getMessage());
			
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;
		}
	}
	
	private static synchronized void init() {
		if(!initialized) {
			for(int i = 9001; i < 9010; ++i) {
				socketUsage.add(new SocketUsagePair(i, -1));
			}
			
			initialized = true;
		}
	}
	
	public static synchronized int getQuietPort() {
		int minUsage = 9999;
		int minPort = 0;
		SocketUsagePair temp;
		
		for(int i = 0; i < socketUsage.size(); ++i) {
			temp = socketUsage.get(i);
			if(temp.usage() < minUsage && temp.usage() >= 0) {
				minUsage = temp.usage();
				minPort = temp.portNum();
			}
		}
		
		return minPort;
	}

}
