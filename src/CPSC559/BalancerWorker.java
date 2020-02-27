package CPSC559;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//Class to handle all the client requests to the load balancer
public class BalancerWorker implements Runnable {

	protected Socket clientSocket = null;
	protected int clientID;
	
	public BalancerWorker(Socket clientSocket, int clientID) {
		this.clientSocket = clientSocket;
		this.clientID = clientID;
	}
	
	public void run() {
		try {
			System.out.println("New client" + this.clientID + "!");
			//get streams
			InputStream input = clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();
			
			output.write(("BalanceWorker is running.\nYour ID is: " + this.clientID + '\0').getBytes());
			output.close();
			input.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
