package p2p_gnutella;

/**
 * This is a the IndexHandler class.
 * Mainly perform the Query request.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-10-18
 * */

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

public class IndexHandler implements Runnable {

	private Socket socket;
	public Gson gson;
	public DataInputStream dis;
	public DataOutputStream dos;
	public long endTime;
	private static Object lock = new Object(); 

	public int threadIndex;

	public IndexHandler(int i) {
		gson = new Gson();
		threadIndex = i;
	}

	public void run() {
		try {
			socket = Client.socket[threadIndex];
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			int commandIndex;
			do {
				commandIndex = Integer.parseInt(dis.readUTF());
				// the process is protected by a mutex lock
				// which could avoid errors
				synchronized (lock){
				switch (commandIndex) {
				case 2:
					Message tempM = gson.fromJson(dis.readUTF(), Message.class);
					tempM.TTLdecrease();
					// tempM.printMessage();
					PeerInfo p = new PeerInfo();
					p = gson.fromJson(dis.readUTF(), p.getClass());
					// p.printPeer();
					
					// check the MessageArray
					// if yes, will not forward this message anymore
					if (Client.checkMessageArray(tempM)) {
						tempM.currentTTL = 0;
						break;
					}
					// if no, store the message in the MessageArray
					if (!Client.checkMessageArray(tempM)) {
						Client.messageArray[Client.messageNumber] = tempM;
						Client.upstreamArray[Client.messageNumber] = p;
						Client.messageNumber++;
					}
					Gson g = new Gson();
					// forward message to all it's neighbors
					if (tempM.currentTTL != 0) {
						for (int i = 0; i < Client.neighborsCount; i++) {
							// not send to its upstream
							if (p.peerName.equals(Client.neighbors[i].peerName) == false) {

								DataOutputStream dos = new DataOutputStream(
										Client.socket[i].getOutputStream());
								dos.writeUTF("2");
								dos.flush();
								// send message
								String sendBuffer = g.toJson(tempM);
								dos.writeUTF(sendBuffer);
								dos.flush();
								// send upstream information
								sendBuffer = g.toJson(Client.self);
								dos.writeUTF(sendBuffer);
								dos.flush();
							}
						}
					}

					// send hit query back
					// 1. find if the current peer has the target file.
					String searchFile = tempM.FileName;
					boolean searchResult = false;
					
					if (Client.sharedFiles.containsKey(searchFile))
						searchResult = true;
					else if (Client.downloadFiles.containsKey(searchFile)
							&& Client.downloadFiles.get(searchFile).conState.equals("Valid"))
						searchResult = true;

					//if (searchResult){
					//	// System.out.println("I have " + searchFile);
					//}

					int connectionIndex = 0;
					// 2. find the upstream connection
					for (int i = 0; i < Client.neighborsCount; i++) {
						if (Client.neighbors[i].peerName.equals(p.peerName)) {
							connectionIndex = i;
						}
					}

					dos = new DataOutputStream(
							Client.socket[connectionIndex].getOutputStream());
					// System.out.println("2 to 3");
					dos.writeUTF("3");
					dos.flush();

					// 3. send back to the upstream peer.
					HitMessage h = new HitMessage(tempM.messageID, searchResult,
							Client.self);

					String sendBuffer = g.toJson(h);
					dos.writeUTF(sendBuffer);
					dos.flush();

					break;
					
				case 3:
					//System.out.println("3");
					HitMessage receivedHitMessage = new HitMessage();
					receivedHitMessage = gson.fromJson(dis.readUTF(),
							receivedHitMessage.getClass());

					// if current peer is not the sender, we continue sending
					// this hit message to upstream.
					int previousIndex = -1;
					// 1. decide whether the current peer is the source.
					if (receivedHitMessage.m.peerID.peerName
							.equals(Client.self.peerName) == false) {
						// 2. find upstream peer index
						for (int i = 0; i < Client.messageNumber; i++) {
							if (Client.messageArray[i].messageID
									.isEqual(receivedHitMessage.m)) {
								previousIndex = i;
							}
						}
					}

					int chooseSocket = -1;

					// find the upstream client's socket index
					if (previousIndex != -1) {
						for (int i = 0; i < Client.neighborsCount; i++) {
							if (Client.neighbors[i].peerName
									.equals(Client.upstreamArray[previousIndex].peerName))
								chooseSocket = i;
						}
					}

					// if current peer is the sender and the hit message has the file
					if (Client.self.peerName
							.equals(receivedHitMessage.m.peerID.peerName)
							&& receivedHitMessage.flag) {
						// System.out.println(Client.hitCount);
						// Write response time to a TXT file
						if (Client.startTime != 0){
							Client.hitCount ++;
							endTime = System.currentTimeMillis();
							double eclipsedTime = endTime - Client.startTime;
							File writename = new File(Client.self.peerPath + "/" 
							    + "test_result.txt");
							if (!writename.exists()){
								writename.createNewFile();
							}
							FileWriter fw = new FileWriter(writename,true);
							fw.write("\r");fw.write("\n");
							fw.write(String.valueOf(eclipsedTime));
							fw.close();
							// System.out.println("Time " + eclipsedTime);
						}else{
							System.out.println(receivedHitMessage.target.peerName
									+ " got the file.");
							Client.hitCount ++;
						}
						// previousIndex = -1;
					}
					
					// System.out.println("previous index is " + previousIndex);
					if (chooseSocket != -1) {
						dos = new DataOutputStream(
								Client.socket[chooseSocket].getOutputStream());

						dos.writeUTF("3");
						dos.flush();
						// 3. send back to the upstream peer.
						sendBuffer = gson.toJson(receivedHitMessage);
						dos.writeUTF(sendBuffer);
						dos.flush();
					}					
					break;
					
				case 4:
					InvalidateMessage tempInvM = gson.fromJson(dis.readUTF(), 
							InvalidateMessage.class);
					tempInvM.TTLdecrease();
					//tempInvM.printInvalidateMessage();
					//System.out.println(tempInvM.messageID.sequenceNumber);
					PeerInfo tempP = new PeerInfo();
					tempP = gson.fromJson(dis.readUTF(), tempP.getClass());
					// p.printPeer();
					
					// check the MessageArray
					// if yes, will not forward this message anymore
					if (Client.checkInvalidMsgArray(tempInvM)) {
						tempInvM.TTL = 0;
						break;
					}
					// if no, store the message in the MessageArray
					if (!Client.checkInvalidMsgArray(tempInvM)) {
						Client.invalidMsgArray[Client.invalidMsgNumber] = tempInvM;
						Client.invalidUpsArray[Client.invalidMsgNumber] = tempP;
						Client.invalidMsgNumber ++;
					}
					Gson g1 = new Gson();
					// forward message to all it's neighbors
					if (tempInvM.TTL != 0) {
						for (int i = 0; i < Client.neighborsCount; i++) {
							// not send to its upstream
							if (tempP.peerName.equals(Client.neighbors[i].peerName) == false) 
							{
								DataOutputStream dos = new DataOutputStream(
										Client.socket[i].getOutputStream());
								dos.writeUTF("4");
								dos.flush();
								// send message
								String sendBuffer1 = g1.toJson(tempInvM);
								dos.writeUTF(sendBuffer1);
								dos.flush();
								// send upstream information
								sendBuffer1 = g1.toJson(Client.self);
								dos.writeUTF(sendBuffer1);
								dos.flush();
							}
						}
					}
					
					// find if the current peer has the target file in Downloaded file folder
					// and check the Version Number
					String tempFile = tempInvM.fileInfo.fileName;
					FileInfo fInfo = new FileInfo();
					fInfo = Client.downloadFiles.get(tempFile);
					if (!Client.downloadFiles.containsKey(tempFile)) {
						System.out.println("I don't Have this File!");
						break;
						}
					if (Client.downloadFiles.containsKey(tempFile)
							&& fInfo.versionNum != tempInvM.fileInfo.versionNum) {
						Client.downloadFiles.get(tempFile).conState = "Invalid";
						System.out.println("Consistency State for " + tempFile +
								" is: " + Client.downloadFiles.get(tempFile).conState);
					} else if (fInfo.versionNum == tempInvM.fileInfo.versionNum)
						System.out.println("Consistency State for " + tempFile +
								" is: " + Client.downloadFiles.get(tempFile).conState);
					break;
				}}
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
