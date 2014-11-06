package p2p_gnutella;

/**
 * This is a the peer class.
 * Store all information of a peer.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-09-18
 * */

import java.io.File;

public class PeerInfo {

	public String peerName;
	public String peerIP;
	public String peerPort;
	public String peerPath;

	public PeerInfo() {	
	}
	
	
	public PeerInfo(String peername, String peerip, String peerport) {
		peerName = peername;
		peerIP = peerip;
		peerPort = peerport;
		File dir = new File("");
		String currentPath = dir.getAbsolutePath();	
		peerPath = currentPath + "/" + peerName;
	}

	public PeerInfo(PeerInfo p) {
		this.peerName = p.peerName;
		this.peerIP = p.peerIP;
		this.peerPort = p.peerPort;
		this.peerPath = p.peerPath;
	}
	
	
	public void printPeer() {
		System.out.println("Peer Name:" + peerName + "  IP:" + peerIP 
				+ "  Port:" + peerPort);
	}
}
