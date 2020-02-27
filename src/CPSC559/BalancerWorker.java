import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//Class to handle all the client requests to the load balancer
public class BalancerWorker implements Runnable {

	protected Socket clientSocket = null;
	
	public BalancerWorker(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		try {
			System.out.println("New client!");
			//get streams
			InputStream input = clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();
			
			output.write(("HTTP/1.1 \n\nBalanceWorker is running").getBytes());
			output.close();
			input.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
