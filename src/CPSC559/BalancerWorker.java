package CPSC559;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

//Class to handle all the client requests to the load balancer
public class BalancerWorker implements Runnable {

	protected Socket clientSocket = null;
	protected Socket dbSocket = null;
	protected int clientID;
	
	protected boolean isRunning = true;
	
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
				if(requestFromClient.startsWith("s_") || requestFromClient.startsWith("u_")) { //read only. Any servers can handle
					this.dbSocket = new Socket("localhost", UsageChecker.getQuietPort());
				}
				else { //write requests. Send to leader server
					//TODO get leader portNum
				}
				
				this.fromDB = new BufferedReader(new InputStreamReader(dbSocket.getInputStream()));
				this.toDB = new PrintWriter(dbSocket.getOutputStream());
				
				this.toDB.print(requestFromClient);
				this.toDB.flush();
				
				this.toDB.close();
				this.fromDB.close();
				
				String response = this.fromDB.readLine();
				//TODO check for timeout
				
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
