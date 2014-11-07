package p2p_gnutella;

/**
 * This is a the Message class.
 * Store required information for a message.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-10-18
 * */

public class Message {
	public MessageID messageID;
	public int currentTTL;
	public int maxTTL;
	public String FileName;
	
	public Message() {
	}

	public Message(MessageID mID, int ttl, String fn) {
		messageID = mID;
		currentTTL = ttl;
		maxTTL = ttl;
		FileName = fn;
	}

	public void printMessage() {
		System.out.println("message information: ");
		System.out.println("MessageID : " + messageID.peerID.peerName + "-"
				+ messageID.peerID.peerIP + "-" + messageID.peerID.peerPort
				+ "-" + messageID.sequenceNumber);
		System.out.println("TTL : " + currentTTL);
		System.out.println("Search File : " + FileName);
	}

	public void TTLdecrease() {
		currentTTL += -1;
	}
	
	public boolean isEqual(Message m ) {
		String name1 = this.messageID.peerID.peerName;
		int sequence1 = this.messageID.sequenceNumber;
		
		String name2 = m.messageID.peerID.peerName;
		int sequence2 = m.messageID.sequenceNumber;
		
		if(name1.equals(name2) && sequence1==sequence2)
			return true;
		else
			return false;
		
	}
}