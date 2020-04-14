package CPSC559;

import java.util.List;
import java.util.Random;
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
import java.util.Date;

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
	protected boolean checkerRunning = true;
	
	public UsageChecker(int id, int numOfReplicas) {
		if(!initialized) {
			init(numOfReplicas);
		}
		this.id = id;
		this.portNum = socketUsage.get(id).portNum();
	}
	
	@Override
	public void run() {	
		while(checkerRunning) {
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
					if(this.portNum == LoadBalancer.getLeader()) {
						LoadBalancer.clearLeader(this.portNum);
						LoadBalancer.setLeader(leaderElection(2));
					}
					try {
						Thread.sleep(5*1000);
					}
					catch (Exception e2) {
						System.err.println("Thread is mad becuase it's sleep got interupted :(");
					}
					connectionGood = false;
				}
			}
			try {
				Thread.sleep(1000*5);
			} catch(InterruptedException e) {
				System.err.println("Thread is mad becuase it's sleep got interupted :(");
			}
			
			this.updateUsage();
			if(connectionGood) {
				if(!LoadBalancer.hasLeader()) {
					LoadBalancer.setLeader(leaderElection(3));
				}
			}
		}
		
	}
	
	private void updateUsage() {
		this.toDB.println("l");
		this.toDB.flush();
		String response;
		
		try {
			response = this.fromDB.readLine();
			System.out.println("response of usage: " + response);
			
			socketUsage.get(this.id).setUsage(Integer.parseInt(response));
			
		} catch (SocketTimeoutException e) {
			//Set current usage to -1
			if(this.portNum == LoadBalancer.getLeader()) {
				LoadBalancer.clearLeader(this.portNum);
				LoadBalancer.setLeader(leaderElection(4));
			}
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;


		} catch (Exception e) {
			System.out.println("Unable to read usage response from server.");
			System.err.println(e.getMessage());
			
			//TODO remote restart server?
			if(this.portNum == LoadBalancer.getLeader()) {
				LoadBalancer.clearLeader(this.portNum);
				LoadBalancer.setLeader(leaderElection(5));
			}
			
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;

		}
	}
	
	private static synchronized void init(int numOfReplicas) {
		if(initialized) {
			return;
		}
		initialized = true;
		for(int i = 9001; i < (9001 + numOfReplicas); ++i) {
			socketUsage.add(new SocketUsagePair(i, -1));
		}

	}
	
	public static synchronized int getQuietPort() {
		/*
		int minUsage = 9999;
		int minPort = 0;
		SocketUsagePair temp;
		
		for(int i = 0; i < socketUsage.size(); ++i) {
			temp = socketUsage.get(i);
			System.out.println(temp.usage());
			if(temp.usage() < minUsage && temp.usage() >= 0) {
				minUsage = temp.usage();
				minPort = temp.portNum();
			}
		}
		*/
		Random rand = new Random();
		int port = 9001;
		while(true) {
			SocketUsagePair s = socketUsage.get(rand.nextInt(socketUsage.size()));
			if (s.usage() != -1) {
				port = s.portNum();
				break;
			}
		}
		
		return port;
	}

	public static synchronized int leaderElection() {
		System.out.println("Starting leader election.");
		int newPort = -1;
		for(int i = 0; i < socketUsage.size(); ++i) {
			//Get lowest ID socket, which is first alive socket.
			if(socketUsage.get(i).usage() >= 0){
				newPort = socketUsage.get(i).portNum();
				System.out.println(newPort + " is the new leader!");
				return newPort;
			}
		}
		
		return newPort;
	}
	
	public static synchronized int leaderElection(int callFrom) {
		System.out.println("Starting leader election from" + callFrom);
		int newPort = -1;
		for(int i = 0; i < socketUsage.size(); ++i) {
			//Get lowest ID socket, which is first alive socket.
			if(socketUsage.get(i).usage() >= 0){
				newPort = socketUsage.get(i).portNum();
				System.out.println(newPort + " is the new leader!");
				return newPort;
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
	
	private void kill() {
		this.checkerRunning = false;
	}
}
