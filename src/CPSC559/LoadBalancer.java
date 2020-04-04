package CPSC559;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//import sun.tools.tree.ThisExpression;

import java.io.IOException;

//Class in charge of load balancer server
public class LoadBalancer implements Runnable{
	
	protected int balancerPort = 9000;
	protected ServerSocket balancerSocket = null;
	protected Thread runningThread = null;
	protected boolean balancerRunning = true;
	protected int clientCount = 0;
	private static int leaderPort = 9001;
	
	
	public LoadBalancer() {
	}
	
	public LoadBalancer(int port) {
		this.balancerPort = port;
	}
	
	
	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		
		int numberOfReplicas = 3;
		
		//Start the usage checker threads
		for(int i = 0; i < numberOfReplicas; ++i) {
			new Thread(new UsageChecker(i, numberOfReplicas)).start();
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
			
			
			//send the request to a new thread
			new Thread(new BalancerWorker(clientSocket, ++clientCount)).start();
		}
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
	
	public static synchronized boolean hasLeader() {
		return leaderPort == -1;
	}
	
	public static synchronized void clearLeader(int processLeader) {
		if(processLeader == leaderPort) {
			leaderPort = -1;
		}
	}
	
	public static synchronized void setLeader(int newPort) {
		leaderPort = newPort;
	}
	
	public static synchronized int getLeader() {
		return leaderPort;
	}

}
