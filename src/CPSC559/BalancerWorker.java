package CPSC559;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
							//Random rand = new Random();
							//talkTo = rand.nextInt(3) + 9001;
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
						
						String report = response.split("%")[0];
						String command = (response.split("%")[1]).split(";")[0];
						String data = "";
						if(response.indexOf(";") != response.length() - 1) {
							data = (response.split("%")[1]).split(";")[1];
						}

						if(requestFromClient.equals(command)) {
							if(command.startsWith("s_")) {

								if(report.equals("ack")) {
									response = "The search was successful. Here are the results\n" + data;
								}
								else {
									response = "The search was unsuccessful.";
								}
							}
							else if(command.startsWith("u_")) {

								if(report.equals("ack")) {
									response = "The search was successful. Here are the results\n" + data;
								}
								else {
									response = "The search was unsuccessful.";
								}
							}
							else if(command.startsWith("b_")) {
								if(report.equals("ack")) {
									response = "The book was borrowed.";
								}
								else {
									response = "The  book could not be borrowed.";
								}
							}
							else if(command.startsWith("r_")) {
								if(report.equals("ack")) {
									response = "The book was returned.";
								}
								else {
									response = "The book could not be returned.";
								}
							}
							else if(command.startsWith("f_")) {
								if(report.equals("ack")) {
									response = "The fine was applied.";
								}
								else {
									response = "The fine could not be applied.";
								}
							}

						}
						else {
							//Stuff went down, send request to new server if possible or retry, @nick help me here
							//Send request to next server. 
						}

					} catch (SocketTimeoutException e) {
						//Set current port to DC
						UsageChecker.dcPort(talkTo);
						this.toDB.close();
						this.fromDB.close();
						this.dbSocket.close();
						
						//if leader, elect new leader
						if(connectedToLeader) {
							LoadBalancer.clearLeader(talkTo); //remove leader from LB
							UsageChecker.dcPort(talkTo); //remove leader from active server list
							
							LoadBalancer.setLeader(UsageChecker.leaderElection());
							
							/*
							int newLead = LoadBalancer.getLeader() + 1;
							if (newLead > 9003) {
								newLead = 9001;
							}
							LoadBalancer.setLeader(newLead);*/
							
							connectedToLeader = false;
						}
					}
				}
				
				
				this.toDB.close();
				this.fromDB.close();
				
				this.toClient.println(response);
				this.toClient.println("");
				this.toClient.flush();	
			}
			
			this.fromClient.close();
			this.toClient.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
