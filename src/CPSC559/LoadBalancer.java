import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

//Class in charge of load balancer server
public class LoadBalancer implements Runnable{
	
	protected int balancerPort = 9000;
	protected ServerSocket balancerSocket = null;
	protected Thread runningThread = null;
	protected boolean balancerRunning = true;
	protected int clientCount = 0;
	protected List<SocketUsagePair> socketUsage = new ArrayList<SocketUsagePair>();
	
	
	public LoadBalancer(int port) {
		this.balancerPort = port;
	}
	public LoadBalancer() {
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
			
			
			//send the request to a new thread
			new Thread(new BalancerWorker(clientSocket, ++clientCount)).start();
		}
	}
	
	public Socket getQuietSocket() {
		int minUsage = 9999;
		Socket minSocket = null;
		for(int i = 0; i < socketUsage.size(); ++i) {
			if(socketUsage.get(i).usage() < minUsage ) {
				minSocket = socketUsage.get(i).db();
				minUsage = socketUsage.get(i).usage();
			}
		}
		
		return minSocket;
	}
	
	private void getUsage() {
		for(int i = 0; i < socketUsage.size(); ++i) {
			Socket dbSocket = socketUsage.get(i).db();
			
			
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

}
