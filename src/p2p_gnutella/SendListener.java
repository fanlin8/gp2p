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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SendListener implements Runnable {
	
	private Socket socket;
	public DataOutputStream dos;
	public DataInputStream dis;

	@Override
	public synchronized void run() {
		try {
			System.out.println(Integer.parseInt(Client.self.peerPort) + 1);
			Client.downloadSSocket = new ServerSocket(Integer.parseInt(Client.self.peerPort) + 1);
			while(true){
				
				socket = Client.downloadSSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				String fPath = null;
				
				String downloadFileName = dis.readUTF();
				System.out.println("Sending File: " + downloadFileName);
				
				for (int i = 0; i < Client.sharedFileList.length; i++) {
					if (Client.sharedFileList[i].equals(downloadFileName))
						fPath = "Shared";
				}
				for (int i = 0; i < Client.downloadFileList.length; i++) {
					if (Client.downloadFileList[i].equals(downloadFileName))
						fPath = "Downloads";
				}
				
				String filePath = Client.self.peerPath + "/" + fPath 
						 + "/" + downloadFileName;
				File file = new File(filePath);
				DataInputStream fis = new DataInputStream(
						new BufferedInputStream(new FileInputStream(filePath)));
				
				dos.writeUTF(file.getName());
				dos.flush();
	            dos.writeLong((long) file.length());   
	            dos.flush(); 
	            
	            int buffferSize = 10240;  
	            byte[]buf = new byte[buffferSize];

	            // send the file
	            while (true) {   
	                int read = 0;   
	                if (fis!= null) {   
	                  read = fis.read(buf);   
	                }   
	  
	                if (read == -1) {   
	                  break;   
	                }   
	                dos.write(buf, 0, read);   
	              }
	              dos.flush();
	              fis.close();
	              socket.close();
	              System.out.println("Sending Complete!");
				
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
