package CPSC559;

public class SocketUsagePair {
	private int usage;
	private final int portNum;
	
	public SocketUsagePair(int portNum, int usage) {
		this.usage = usage;
		this.portNum = portNum;
	}
	
	public int usage() {
		return this.usage;
	}
	
	public void setUsage(int usage) {
		this.usage = usage;
	}
	
	public int portNum() {
		return this.portNum;
	}
	
}
