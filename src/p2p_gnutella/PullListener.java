package p2p_gnutella;

/**
 * This is a the PullListener class.
 * Listening to any pull request from other peers.
 * And will return the original file version number.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-11-04
 * */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;

public class PullListener implements Runnable {

	private Socket socket;
	public Gson gson;
	public DataOutputStream dos;
	public DataInputStream dis;
	
	@Override
	public synchronized void run() {
		try {
			Client.pullSSocket = new ServerSocket(Integer.parseInt(Client.self.peerPort) + 20);
			
			while(true){
				
				socket = Client.pullSSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				gson = new Gson();
				int version;
				
				String pullFileName = dis.readUTF();
				
				// send back the pull result
				if (Client.sharedFiles.containsKey(pullFileName)) {
					version = Client.sharedFiles.get(pullFileName).versionNum;
					dos.writeInt(version);
					dos.flush();
					} else {
						System.out.println("I don't have Such File!");
					}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
