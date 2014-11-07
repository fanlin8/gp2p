package p2p_gnutella;

/**
 * This is the InvalidateMessage class.
 * Store required information for a invalidate Message.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-11-06
 * */

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
