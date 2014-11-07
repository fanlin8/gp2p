package p2p_gnutella;

public class InvalidateMessage {
	public MessageID messageID;
	public FileInfo fileInfo;
	public int TTL;
	
	public InvalidateMessage() {
	}
	
	public InvalidateMessage(MessageID mID, FileInfo fInfo, int ttl) {
		messageID = mID;
		fileInfo = fInfo;
		TTL = ttl;
	}

	public void printInvalidateMessage() {
		System.out.println("Invalidate information: ");
		System.out.println("File: " + fileInfo.fileName + " from: "
				+ fileInfo.originalServer.peerName + " version: " + 
				fileInfo.versionNum + " TTR: " + fileInfo.TTR);
	}

	public void TTLdecrease() {
		TTL += -1;
	}	
}
