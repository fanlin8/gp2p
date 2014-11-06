package p2p_gnutella;

/**
 * This is a the HitMessage class.
 * Store required information for a hit message.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-10-18
 * */

public class HitMessage {
	public MessageID m;
	public boolean flag;
	public PeerInfo target;

	public HitMessage() {

	}

	public HitMessage(MessageID mID, boolean fl, PeerInfo p) {
		m = mID;
		flag = fl;
		target = p;
	}

	public void printHitMessage() {
		System.out.println("Hit message source " + m.peerID.peerName + " ");
		if(flag)
			System.out.println("file found");
		else
			System.out.println("file not found");
		System.out.println("from peer : " + target.peerName);
	}

}