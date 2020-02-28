import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import CPSC559.BalancerWorkerToDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class LoadBalToDB implements Runnable {
	
	protected int balancerPort = 9001;
	protected ServerSocket balancerSocket = null;
	protected Thread runningThread = null;
	protected boolean balancerRunning = true;
	protected int clientCount = 0;
	protected List<SocketUsagePair> socketUsage = new ArrayList<SocketUsagePair>();

	public LoadBalToDB() {
	}
	
	public LoadBalToDB(int port) {
		this.balancerPort = port;
	}
	
	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		//Starting the server on the specified port
		try {
			this.balancerSocket = new ServerSocket(this.balancerPort);
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot open port " + this.balancerPort,e);
		}
		
		while(balancerRunning) {
			Socket clientSocket = null;
			//Accept incoming connection request
			try {
				clientSocket = this.balancerSocket.accept();
			}
			catch (IOException e) {
				if(!balancerRunning) {
					System.out.println("Load balancer has stopped.");
					return;
				}
				throw new RuntimeException("Error accepting client connection.", e);
			}
			
			BalancerWorkerToDB newDBClient = new BalancerWorkerToDB(clientSocket, ++clientCount);
			
			socketUsage.add(new SocketUsagePair(newDBClient, 0, clientCount));
			
			//send the request to a new thread
			new Thread(newDBClient).start();
		}

	}
	
	private BalancerWorkerToDB getQuietSocket() {
		int minUsage = 9999;
		BalancerWorkerToDB minSocket = null;
		for(int i = 0; i < socketUsage.size(); ++i) {
			if(socketUsage.get(i).usage() < minUsage ) {
				minSocket = socketUsage.get(i).db();
				minUsage = socketUsage.get(i).usage();
			}
		}
		
		return minSocket;
	}
	
	private synchronized void updateUsage() {
		for(int i = 0; i < socketUsage.size(); ++i) {
			BalancerWorkerToDB dbSocket = socketUsage.get(i).db();
			
			int usage;
			
			try {
				
				usage = dbSocket.getUsage();
				
				socketUsage.get(i).setUsage(usage);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public String communicateWithDB(String message) {
		
		String response = "";
		
		try {
			BalancerWorkerToDB quietSocket = getQuietSocket();
			
			response = quietSocket.communicate(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
		
	}
	
	public synchronized void stop() {
		this.balancerRunning = false;
		try {
			this.balancerSocket.close();
		}
		catch (IOException e) {
			throw new RuntimeException("Error closing the load balancer.", e);
		}
	}
	

}
