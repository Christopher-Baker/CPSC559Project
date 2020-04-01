package CPSC559;

import java.util.List;
import java.util.ArrayList;
import java.net.ResponseCache;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;

import CPSC559.LoadBalancer;
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
		if(!initialized) {
			initialized = true;
			init();
		}
		this.id = id;
		this.portNum = socketUsage.get(id).portNum();
	}
	
	@Override
	public void run() {
		
		while(!connectionGood) {
			try{
				dbSocket = new Socket("localhost", this.portNum);
				dbSocket.setSoTimeout(10*1000);
				
				toDB = new PrintWriter(dbSocket.getOutputStream());
				fromDB = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));
				connectionGood = true;
			}
			catch (Exception e) {
				System.out.println("Waiting on port " + this.portNum);
				connectionGood = false;
			}
		}
		
		this.updateUsage();
		if(connectionGood) {
			if(!LoadBalancer.hasLeader()) {
				leaderElection();
			}
		}
		

	}
	
	private void updateUsage() {
		this.toDB.println("l");
		this.toDB.flush();
		String response;
		
		try {
			response = this.fromDB.readLine();
			
			socketUsage.get(this.id).setUsage(Integer.parseInt(response));
			
		} catch (SocketTimeoutException e) {
			//Set current usage to -1
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;
		} 
		
		catch (Exception e) {
			System.out.println("Unable to read usage response from server.");
			System.err.println(e.getMessage());
			
			//TODO remote restart server?
			
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;
		}
	}
	
	private static synchronized void init() {
		
		for(int i = 9001; i < 9010; ++i) {
			socketUsage.add(new SocketUsagePair(i, -1));
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

	public static synchronized int leaderElection() {
		int minUsage = 9999;
		int newPort = -1;
		for(int i = 0; i < socketUsage.size(); ++i) {
			if(socketUsage.get(i).usage() < minUsage && socketUsage.get(i).usage() > 0){
				minUsage = socketUsage.get(i).usage();
				newPort = socketUsage.get(i).portNum();
			}
		}
		
		return newPort;
	}

	public static synchronized void dcPort(int port) {
		for(int i = 0; i < socketUsage.size(); ++i) {
			if(socketUsage.get(i).portNum() == port) {
				socketUsage.get(i).setUsage(-1);
			}
		}
	}
}
