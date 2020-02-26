import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	public static void printHelpOutput() {
		System.out.println("quit, (q)");
		System.out.println("search [String bookName], (s [String bookName])");
		System.out.println("borrow [String bookName], (b [String bookName])");
		System.out.println("return [String bookname], (r [String bookName])");
		System.out.println("payFees [String patronName], (p [String patronName])");
		System.out.println("kill [int servNum], (k [int servNum])");
		System.out.println("\t1 - replicate 1");
		System.out.println("\t2 - replicate 2");
		System.out.println("\t3 - replicate 3");
		System.out.println("\t4 - user 'DB'");
		System.out.println("fixServer [int servNum], (f [int servNum])");
		System.out.println("\t1 - replicate 1");
		System.out.println("\t2 - replicate 2");
		System.out.println("\t3 - replicate 3");
		System.out.println("\t4 - user 'DB'");
	}
	
	public static void invalidCommand() {
		System.out.println("Not a recognized command, here is the list of valid ones");
		Client.printHelpOutput();
	}
	public static void sendRequest(PrintWriter p, String r) {
		p.print(r);
		p.flush();
	}
	
	public static void handleServerResponse(BufferedReader responseStream) {
		String response;
		boolean keepRunning = true;
		while(keepRunning == true) {
			try {
				response = responseStream.readLine();
				if(response != null) {
					System.out.println(response);
				}
				else {
					keepRunning = false;
				}
			}
			catch(IOException e) {
				System.out.print("Unable to read server's response");
			}
		}
	}
	
	//Assuming the server is on local host port 9000
	public static void main(String args[]) {
		String host = "localhost";
		int portNum = 9000;
		
		boolean connectionGood = false;
		
		Socket librarySocket = null;
	    PrintWriter toTheSocket = null;
	    BufferedReader responseFromSocket = null;
	    
		try {
			System.out.println("Connecting to the library server");
			librarySocket = new Socket(host, portNum);
			toTheSocket = new PrintWriter(librarySocket.getOutputStream());
			responseFromSocket = new BufferedReader(new InputStreamReader(librarySocket.getInputStream()));
			connectionGood = true;
		}
		catch(IOException e1) {
			System.out.println("Could not find server, check if it is running");
		}
		
		
		if(connectionGood == true) {
			System.out.println("What is your name?");
			Scanner kbReader = new Scanner(System.in);
			String usr = kbReader.nextLine();
			
			System.out.println("Welcome to The library system " + usr + "!");
			Client.printHelpOutput();
			
			boolean quitApplication = false;
			while(quitApplication == false) {
				String usrInput = kbReader.nextLine();
				String[] usrInputArr = usrInput.split(" ");
				if(usrInputArr.length == 1) {
					if(usrInput.equalsIgnoreCase("quit") || usrInput.equalsIgnoreCase("q")) {
						quitApplication = true;
					}
					else {
						Client.invalidCommand();
					}
				}
				else if (usrInputArr.length == 0 || usrInputArr.length > 2){
					Client.invalidCommand();
				}
				else if(usrInputArr.length == 2) {
					boolean commandValid = true;
					String command = usrInputArr[0];
					String option = usrInputArr[1];
					
					if(command.equalsIgnoreCase("search") || command.equalsIgnoreCase("s")) {
						String request = "s_" + usr + "_" + option + "\n";
						Client.sendRequest(toTheSocket, request);
					}
					else if(command.equalsIgnoreCase("borrow") || command.equalsIgnoreCase("b")) {
						String request = "b_" + usr + "_" + option + "\n";
						Client.sendRequest(toTheSocket, request);
					}
					else if(command.equalsIgnoreCase("return") || command.equalsIgnoreCase("r")) {
						String request = "r_" + usr + "_" + option + "\n";
						Client.sendRequest(toTheSocket, request);
					}
					else if(command.equalsIgnoreCase("payFees") || command.equalsIgnoreCase("p")) {
						String request = "p_" + usr + "_" + option + "\n";
						Client.sendRequest(toTheSocket, request);
					}
					else if(command.equalsIgnoreCase("kill") || command.equalsIgnoreCase("k")) {
						try {
							int optionNum = Integer.parseInt(option);
							if(optionNum == 0 || optionNum > 4) {
								throw new NumberFormatException();
							}
							String request = "k_"+ optionNum + "\n";
							Client.sendRequest(toTheSocket, request);
						}
						catch(NumberFormatException e) {
							commandValid = false;
							Client.invalidCommand();
						}
					}
					else if(command.equalsIgnoreCase("fixServer") || command.equalsIgnoreCase("f")) {
						try {
							int optionNum = Integer.parseInt(option);
							if(optionNum == 0 || optionNum > 4) {
								throw new NumberFormatException();
							}
							String request = "f_"+ optionNum + "\n";
							Client.sendRequest(toTheSocket, request);
						}
						catch(NumberFormatException e) {
							commandValid = false;
							Client.invalidCommand();
						}
					}
					else {
						commandValid = false;
						Client.invalidCommand();
					}
					
					if(commandValid == true) {
						Client.handleServerResponse(responseFromSocket);
					}
				}
			}
			
			kbReader.close();
		}
		System.out.println("Exiting");
		try {
			librarySocket.close();
		}
		catch(IOException e) {
			System.out.println("Error while closing IO streams");
		}
		catch(NullPointerException e) {
			//Silence this exception
		}
		
		
		
	}
}
