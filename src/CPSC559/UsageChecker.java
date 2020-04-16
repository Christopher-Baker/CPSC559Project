package CPSC559;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;


public class UsageChecker implements Runnable {

	public static List<SocketUsagePair> socketUsage = new ArrayList<SocketUsagePair>();
	private static boolean initialized = false;
	
	private final int portNum;
	private final int id;
	private Socket dbSocket = null;
	private PrintWriter toDB = null;
	private BufferedReader fromDB = null;
	private boolean connectionGood = false;
	private boolean disrupted = false;
	protected boolean checkerRunning = true;
	protected boolean getNewSockets = false;
	
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
			while(!connectionGood || getNewSockets) {
				try{
					dbSocket = new Socket("localhost", this.portNum);
					dbSocket.setSoTimeout(10*1000);
					
					toDB = new PrintWriter(dbSocket.getOutputStream());
					fromDB = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));

					if(this.disrupted){
						initiateDBSend(); // if the connection recovers, send the database over
					}

					connectionGood = true;
					getNewSockets = false;
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
					disrupted = true;
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
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;
			
			//Do leader election
			if(this.portNum == LoadBalancer.getLeader()) {
				LoadBalancer.clearLeader(this.portNum);
				LoadBalancer.setLeader(leaderElection(4));
			}
			


		} catch (Exception e) {
			System.out.println("Unable to read usage response from server.");
			System.err.println(e.getMessage());

			if(this.portNum == LoadBalancer.getLeader()) {
				LoadBalancer.clearLeader(this.portNum);
				LoadBalancer.setLeader(leaderElection(5));
			}
			
			socketUsage.get(this.id).setUsage(-1);
			connectionGood = false;

		} finally {
			try {
				this.fromDB.close();
				this.toDB.close();
				this.dbSocket.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			getNewSockets = true;
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
		// currently gets a random port, as in our test system all ports have equal usage of 0, so picking the quietest one was not realistic
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
	

	public void initiateDBSend() {
		// if the connection recovers
		try {
			int leaderPort = LoadBalancer.getLeader();
			Socket leaderSocket = new Socket("localhost", leaderPort);
			PrintWriter toLeader = new PrintWriter(leaderSocket.getOutputStream());

			toDB.println("recvDB_Request");
			toDB.flush();

			String response = fromDB.readLine();
			int replyPort = Integer.parseInt(response);

			if (replyPort != -1) {
				Thread.sleep(1000);
				String sendDBRequest = "sendDB_localhost:" + response;
				toLeader.println(sendDBRequest);
				toLeader.flush();
				toLeader.close();
				leaderSocket.close();
				this.disrupted = false;
			} else {
				throw new IllegalArgumentException("Failed to get an available port from remote.");
			}
		} catch (Exception e) {
			System.out.println("Failed to send the database.");
		}
	}
}
