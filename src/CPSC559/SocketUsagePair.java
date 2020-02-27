package CPSC559;

import java.net.Socket;

public class SocketUsagePair {
	private int usage;
	private final Socket dbSocket;
	private final int dbID;
	
	public SocketUsagePair(Socket dbSocket, int usage, int dbID) {
		this.usage = usage;
		this.dbSocket = dbSocket;
		this.dbID = dbID;
	}
	
	public int usage() {
		return this.usage;
	}
	
	public Socket db() {
		return this.dbSocket;
	}
	
	public int dbID() {
		return this.dbID;
	}
}
