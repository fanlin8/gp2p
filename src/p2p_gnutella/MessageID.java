package p2p_gnutella;

/**
 * This is a the MessageID class.
 * Store required information for a message ID.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-10-18
 * */

public class MessageID {
	public PeerInfo peerID;
	public int sequenceNumber;
	public static int globalSequenceNumber = 0;

	public MessageID(PeerInfo p) {
		peerID = new PeerInfo(p);
		sequenceNumber = MessageID.globalSequenceNumber;
		MessageID.globalSequenceNumber++;
	}

	public boolean isEqual(MessageID m) {
		if(this.peerID.peerName.equals(m.peerID.peerName) && this.sequenceNumber==m.sequenceNumber)
			return true;
		else
			return false;
	}
}
