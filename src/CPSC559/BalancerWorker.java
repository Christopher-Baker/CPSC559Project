package CPSC559;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import CPSC559.LoadBalancer;

//Class to handle all the client requests to the load balancer
public class BalancerWorker implements Runnable {

	protected Socket clientSocket = null;
	protected Socket dbSocket = null;
	protected int clientID;
	
	protected boolean isRunning = true;
	private boolean connectedToLeader = false;
	
	protected BufferedReader fromClient = null;
	protected PrintWriter toClient = null;
	
	protected BufferedReader fromDB = null;
	protected PrintWriter toDB = null;
	
	public BalancerWorker(Socket clientSocket, int clientID) {
		this.clientSocket = clientSocket;
		this.clientID = clientID;
	}
	
	public void run() {
		try {
			System.out.println("New client" + this.clientID + "!");

			//get streams
			this.fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.toClient = new PrintWriter(clientSocket.getOutputStream());
			
			while(isRunning) {
				
				String requestFromClient = this.fromClient.readLine();

				int talkTo = -1;
				String response = null;
				boolean haveResponse = false;
				
				
				while(!haveResponse) {
					try {
						if(requestFromClient.startsWith("s_")) { //read only. Any servers can handle
							talkTo = UsageChecker.getQuietPort();
							if(talkTo == LoadBalancer.getLeader()) {
								connectedToLeader = true;
							}
							else {
								connectedToLeader = false;
							}
						}
						else { //write requests. Send to leader server
							connectedToLeader = true;
							talkTo = LoadBalancer.getLeader();
						}
						
						this.dbSocket = new Socket("localhost", talkTo);
						this.dbSocket.setSoTimeout(10*1000);
						
						this.fromDB = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));
						this.toDB = new PrintWriter(dbSocket.getOutputStream());
						
						this.toDB.print(requestFromClient);
						this.toDB.flush();
						
						
						response = this.fromDB.readLine();
						haveResponse = true;
					} catch (SocketTimeoutException e) {
						//Set current port to DC
						UsageChecker.dcPort(talkTo);
						this.toDB.close();
						this.fromDB.close();
						this.dbSocket.close();
						
						//if leader, elect new leader
						if(connectedToLeader) {
							LoadBalancer.clearLeader();
							if(!LoadBalancer.hasLeader()) {
								LoadBalancer.setLeader(UsageChecker.leaderElection());
							}
							
							connectedToLeader = false;
						}
					}
				}
				
				
				this.toDB.close();
				this.fromDB.close();
				
				this.toClient.print(response);
				this.toClient.flush();
				
			}
			
			this.fromClient.close();
			this.toClient.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
