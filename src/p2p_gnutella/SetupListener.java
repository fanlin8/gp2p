package p2p_gnutella;

/**
 * This is a the Listener class.
 * Listening to any request from other peers.
 * Also handle the sending process.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-10-18
 * */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SetupListener implements Runnable{

	private Socket socket;
	public DataOutputStream dos;
	public DataInputStream dis;
	
	@Override
	public void run() {
		try {
			String senderName;
			//System.out.println(Integer.parseInt(Client.self.peerPort));
			Client.setupSSocket = new ServerSocket(Integer.parseInt(Client.self.peerPort));
			
			while(true){
				socket = Client.setupSSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				senderName = dis.readUTF();
				// System.out.println(senderName);
				
				// find the right slot in the static socket array if not a Download request.
				for(int i = 0 ; i < Client.neighborsCount ; i ++ ) {
					if (Client.neighbors[i].peerName.equals(senderName)) {
						Client.socket[i] = socket;
					}
				}			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
}