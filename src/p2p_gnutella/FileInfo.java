package p2p_gnutella;

/**
 * This is the FileInfo class.
 * Store required file information for specific file.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-11-06
 * */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
	
	// method pull() for a specific file
	// act same with the method pull() in class Client.java
	@SuppressWarnings("resource")
	public synchronized void pull() {
		//System.out.println(peerName);
		//System.out.println(fileName);
		
		for (int j = 0; j < 10; j++) {
			try {
				if (Client.peerList[j].peerName.equals(originalServer.peerName)){
					Socket p2pSocket = new Socket(Client.peerList[j].peerIP, 
							Integer.parseInt(Client.peerList[j].peerPort) + 20);
					DataInputStream p2pIn = new DataInputStream(
							p2pSocket.getInputStream());
					DataOutputStream p2pOut = new DataOutputStream(
							p2pSocket.getOutputStream());
					
					p2pOut.writeUTF(fileName);
					p2pOut.flush();					
					int originVersion = p2pIn.readInt();
					
					if (originVersion != versionNum) {
						System.out.println(fileName + " is out of date");
						Client.downloadFiles.get(fileName).conState = "Invalid"; 
					} else {
						System.out.println(fileName + " is newest");
						Client.downloadFiles.get(fileName).conState = "Valid";
						Client.downloadFiles.get(fileName).TTR = Client.TTR;
					}										
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
