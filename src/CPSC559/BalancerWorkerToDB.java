package CPSC559;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//Class to handle all the client requests to the load balancer
public class BalancerWorkerToDB implements Runnable {

	protected Socket clientSocket = null;
	protected int clientID;
	protected boolean isRunning = true;
	
	
	protected InputStream input = null;
	protected OutputStream output = null;
	
	public BalancerWorkerToDB(Socket clientSocket, int clientID) {
		this.clientSocket = clientSocket;
		this.clientID = clientID;
		
		this.input = clientSocket.getInputStream();
		this.output = clientSocket.getOutputStream();
	}
	
	public void run() {
		try {
			System.out.println("New client" + this.clientID + "!");
			//get streams if closed
			if(this.input==null) {
				this.input = clientSocket.getInputStream();
			}
			
			if(this.output==null) {
				this.output = clientSocket.getOutputStream();
			}
			
			this.output.write(("BalanceWorker is running.\nYour ID is: " + this.clientID + '\0').getBytes());
			
			while(isRunning) {
				Thread.sleep(3000);
			}
			
			this.output.close();
			this.output = null;
			this.input.close();
			this.input = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized String communicate(String message) {
		String response = "";
		char incoming;
		
		try {
			
			this.output.write((message + '\0').getBytes());
			
			incoming = (char) this.input.read();
			response += incoming;
			while(incoming != '\0') {
				incoming = (char) this.input.read();
				response += incoming;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	public synchronized int getUsage() {
		int usage;
		try {
			this.output.write(("u_" + '\0').getBytes());
			usage = this.input.read();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return usage;
	}

}
