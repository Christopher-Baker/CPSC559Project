package CPSC559;

import java.net.Socket;

import CPSC559.BalancerWorkerToDB;

public class SocketUsagePair {
	private int usage;
	private final BalancerWorkerToDB dbSocket;
	private final int dbID;
	
	public SocketUsagePair(BalancerWorkerToDB dbSocket, int usage, int dbID) {
		this.usage = usage;
		this.dbSocket = dbSocket;
		this.dbID = dbID;
	}
	
	public int usage() {
		return this.usage;
	}
	
	public void setUsage(int usage) {
		this.usage = usage;
	}
	
	public BalancerWorkerToDB db() {
		return this.dbSocket;
	}
	
	public int dbID() {
		return this.dbID;
	}
}
