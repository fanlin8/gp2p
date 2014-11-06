package p2p_gnutella;

public class FileInfo {
	public String fileName;
	public int versionNum;
	public PeerInfo originalServer;
	public String conState;
	public int TTR;

	public FileInfo() {	
	}
	
	public FileInfo(String s, PeerInfo peer, int ttr) {
		fileName = s;
		versionNum = 0;
		originalServer = new PeerInfo();
		originalServer = peer;
		conState = "Valid";
		TTR = ttr;
	}
	
	public FileInfo(FileInfo file) {
		this.fileName = file.fileName;
		this.originalServer = file.originalServer;
		this.versionNum = file.versionNum;
		this.conState = file.conState;
		this.TTR = file.TTR;
	}
	
	public void versionIncrease() {
		versionNum += 1;
	}
	
	public void printFile() {
		System.out.println("File: " + fileName + " from: "
				+ originalServer.peerName + " version: " + versionNum + " state: "
				+ conState + " TTR: " + TTR);
	}

}
