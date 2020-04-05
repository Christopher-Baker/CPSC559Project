package CPSC559;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

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
				boolean search = false;
				
				
				while(!haveResponse) {
					if (requestFromClient.length() == 0) {
						break; // This is a hack becuase client sends empty query sometimes
					}
					try {
						if(requestFromClient.startsWith("s_") || requestFromClient.startsWith("u_")) { //read only. Any servers can handle
							search = true;
							Random rand = new Random();
							talkTo = rand.nextInt(3) + 9001;
							// talkTo = UsageChecker.getQuietPort();
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
						System.out.println("Sending request: " + requestFromClient + " to port " + talkTo);
						this.dbSocket = new Socket("localhost", talkTo);
						this.dbSocket.setSoTimeout(30*1000);
						
						this.fromDB = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));
						this.toDB = new PrintWriter(dbSocket.getOutputStream());
						
						this.toDB.println(requestFromClient);
						this.toDB.flush();
						
						System.out.println("waiting for response");
						response = this.fromDB.readLine(); //TODO if not begins with requestFromClient, try again
						System.out.println("Got repsonse from server: " + response);
						haveResponse = true;
						if (response.equals("nack")) {
							response = "The server was unable to process this request";
						}
						else if(response.equals("ack")) {
							response = "The server has successfully completed your request";
						}
					} catch (SocketTimeoutException e) {
						//Set current port to DC
						UsageChecker.dcPort(talkTo);
						this.toDB.close();
						this.fromDB.close();
						this.dbSocket.close();
						
						//if leader, elect new leader
						if(connectedToLeader) {
							int newLead = LoadBalancer.getLeader() + 1;
							if (newLead > 9003) {
								newLead = 9001;
							}
							LoadBalancer.setLeader(newLead);
							
							connectedToLeader = false;
						}
					}
				}
				
				
				this.toDB.close();
				this.fromDB.close();
				
				this.toClient.println(response);
				this.toClient.flush();
				
			}
			
			this.fromClient.close();
			this.toClient.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
