package CPSC559;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	public static void printHelpOutput() {
		System.out.println("quit, (q)");
		System.out.println("search,[String bookName] (s,[String bookName])");
		System.out.println("searchUser,[String userLastName] (u,[String UserLastName)");
		System.out.println("borrow,[int userID],[int bookID] (b,[int userID],[int bookID])");
		System.out.println("return,[int bookID] (r,[int bookID])");
		System.out.println("adjustFees,[int userID],[double feeChangeAmount] (f,[int userID],[double feeChangeAmount])");
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
			try {
				while(quitApplication == false) {
					String usrInput = kbReader.nextLine();
					String[] usrInputArr = usrInput.split(",");
					System.out.println(usrInputArr.length);
					if(usrInputArr.length == 1) {
						if(usrInput.equalsIgnoreCase("quit") || usrInput.equalsIgnoreCase("q")) {
							quitApplication = true;
						}
						else {
							Client.invalidCommand();
						}
					}
					else if (usrInputArr.length == 0){
						Client.invalidCommand();
					}
					else {
						String command = usrInputArr[0];
						//String option = usrInputArr[1];
						System.out.println(command);
						
						if (command.equalsIgnoreCase("search") || command.equalsIgnoreCase("s")) {
							if (usrInputArr.length == 2) {
								String request = "s_" + usrInputArr[1] + "\n";
								Client.sendRequest(toTheSocket, request);
								Client.handleServerResponse(responseFromSocket);
							} else {
								Client.printHelpOutput();
							}
							
						}
						else if (command.equalsIgnoreCase("searchUser") || command.equalsIgnoreCase("u")) {
							if (usrInputArr.length == 2) {
								String request = "u_" + usrInputArr[1] + "\n";
								Client.sendRequest(toTheSocket, request);
								Client.handleServerResponse(responseFromSocket);
							} else {
								Client.printHelpOutput();	
							}
						}
						else if (command.equalsIgnoreCase("borrow") || command.equalsIgnoreCase("b")) {
							if (usrInputArr.length == 3) {
								String request = "b_" + usrInputArr[1] + "_" + usrInputArr[2] + "\n";
								Client.sendRequest(toTheSocket, request);
								Client.handleServerResponse(responseFromSocket);
							} else {
								Client.printHelpOutput();
							}	
						}
						else if (command.equalsIgnoreCase("return") || command.equalsIgnoreCase("r")) {
							if (usrInputArr.length == 2) {
								String request = "r_" + usrInputArr[1] + "\n";
								Client.sendRequest(toTheSocket, request);
								Client.handleServerResponse(responseFromSocket);
							} else {
								Client.printHelpOutput();
							}	
						}
						else if  (command.equalsIgnoreCase("adjustFees") || command.equalsIgnoreCase("f")) {
							if (usrInputArr.length == 3) {
								String request = "f_" + usrInputArr[1] + "_" + usrInputArr[2] + "\n";
								Client.sendRequest(toTheSocket, request);
								Client.handleServerResponse(responseFromSocket);
							} else {
								Client.printHelpOutput();
							}
						}
						else if (command.equalsIgnoreCase("kill") || command.equalsIgnoreCase("k")) {
							System.out.println("The kill command is unsupported at this time");
							// TODO Make this and the fix command work or remove them
							// try {
							// 	int optionNum = Integer.parseInt(option);
							// 	if(optionNum == 0 || optionNum > 4) {
							// 		throw new NumberFormatException();
							// 	}
							// 	String request = "k_"+ optionNum + "\n";
							// 	Client.sendRequest(toTheSocket, request);
							// }
							// catch(NumberFormatException e) {
							// 	commandValid = false;
							// 	Client.invalidCommand();
							// }
						}
						else if (command.equalsIgnoreCase("fixServer") || command.equalsIgnoreCase("f")) {
							System.out.println("The fix command is unsupported at this time");
							// try {
							// 	int optionNum = Integer.parseInt(option);
							// 	if(optionNum == 0 || optionNum > 4) {
							// 		throw new NumberFormatException();
							// 	}
							// 	String request = "f_"+ optionNum + "\n";
							// 	Client.sendRequest(toTheSocket, request);
							// }
							// catch(NumberFormatException e) {
							// 	commandValid = false;
							// 	Client.invalidCommand();
							// }
						}
						else {
							Client.invalidCommand();
						}
					}
				}
				
				kbReader.close();
			} catch (Exception e) {
				System.err.println("Error at client: " + e.getMessage());
			}
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
