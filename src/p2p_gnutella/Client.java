package p2p_gnutella;

/**
 * This is a simple Gnutella-style P2P file sharing system in JAVA 7.
 * A Peer could act as both a Sever and a Client.
 * The network is defined by a Configure file.
 * A Peer would maintain a list of its neighbors.
 * The Peer could search a desired file by broadcasting the request to its neighbors.
 * All neighbors will forward the request.
 * If files is found, the destination information will be sent backward.
 * All functions could be done in Concurrency.
 * This System IS tested only on a local host.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-10-18
 * */

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

public class Client {

	public static PeerInfo self;
	public static PeerInfo[] neighbors;
	public static PeerInfo[] peerList;
	public static int neighborsCount;
	public static int hitCount;
	public static long startTime = 0;
	public static String[] sharedFileList;
	public DataOutputStream dos;
	public DataInputStream dis;
	public static ServerSocket setupSSocket;
	public static ServerSocket downloadSSocket;
	public static Socket socket[];
	public Gson gson;
	public static Message[] messageArray;
	public static PeerInfo[] upstreamArray;
	public static int messageNumber;
	private static Object lock = new Object(); 
	private static Object lock1 = new Object();
	
	public static int pushFlag;
	public static int pushTTL;
	public static int pullFlag;
	public static int TTR;
	public static HashMap<String, FileInfo> sharedFiles;
	public static HashMap<String, FileInfo> downloadFiles;

	public Client() {
		initializeClient();
	}

	public String getPort() {
		return self.peerPort;
	}

	public String getIP() {
		return self.peerIP;
	}
	
	public String getName() {
		return self.peerName;
	}
	
	// get peers' list from the configure file
	public static void getPeerList(String inputPath) {
		BufferedReader reader = null;
		try {
			peerList = new PeerInfo[10];
			File file = new File(inputPath);
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			String[] str = null;
			int peerCount = 0;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				peerList[peerCount] = new PeerInfo(str[0], str[1], str[2]);
				peerCount++;
				continue;
			}			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	

	// get a peer's neighbors list from the configure file
	public static void readConfigure(String inputPath) {
		BufferedReader reader = null;
		try {
			neighbors = new PeerInfo[10];
			
			//System.out.println("Please input the name of the peer : ");
			//Scanner input = new Scanner(System.in);
			//String inputString = input.nextLine();

			// set this peer's id, from p1 to p10
			String inputString = "p4";
			File file = new File(inputPath);
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			String[] str = null;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				if (str[0].equals(inputString)) {
					self = new PeerInfo(str[0], str[1], str[2]);
					neighborsCount = str.length - 3;
					self.printPeer();
					reader = new BufferedReader(new FileReader(file));
					int neighborIndex = 0;
					// get neighbor list
					while ((line = reader.readLine()) != null) {
						String[] neighAttr = line.split("\t");
						for (int i = 3; i < neighborsCount + 3; i++) {
							if (str[i].equals(neighAttr[0])) {
								neighbors[neighborIndex] = new PeerInfo(
										neighAttr[0], neighAttr[1], neighAttr[2]);
								neighborIndex++;								
							}							
						}
					}
				}
			}
			// System.out.println(Client.neighborsCount + " Neighbors.");			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// get pull and push setting from the consistency configure file
	public static void readConsistencyConfig(String inputPath) {
		BufferedReader reader = null;
		try {
			File file = new File(inputPath);
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			String[] str = null;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				if (str[0].equals("push")) {
					pushFlag = Integer.parseInt(str[1]);
					pushTTL = Integer.parseInt(str[2]);
				}
				else if (str[0].equals("pull"))
					pullFlag = Integer.parseInt(str[1]);
				else if (str[0].equals("TTR"))
					TTR = Integer.parseInt(str[1]);
				else
					System.out.println("Something Wrong with Your Config File!");
				continue;
			}			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					//System.out.println("PushFlag: " + pushFlag);
					//System.out.println("PushTTL: " + TTR);
					//System.out.println("PullFlag: " + pullFlag);
					//System.out.println("TTR: " + TTR);
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// get each peer's sharing file list.
	public void getSharedList(String inputPath) {

		File file = new File(inputPath);
		if (!file.exists()) {
			file.mkdirs();
		}

		sharedFileList = new String[file.list().length];
		sharedFileList = file.list();

		for (int i = 0; i < sharedFileList.length; i++){
			sharedFiles.put(sharedFileList[i], new FileInfo(
					sharedFileList[i], Client.self, Client.TTR));
		}
		System.out.print("Shared Files: ");
		System.out.println(sharedFiles.keySet());
	}	
	
	public void initializeClient() {

		messageArray = new Message[500];
		upstreamArray = new PeerInfo[500];
		messageNumber = 0;
		gson = new Gson();
		sharedFiles = new HashMap<String, FileInfo>();
		downloadFiles = new HashMap<String, FileInfo>();
		
		File dir = new File("");
		String currentPath = dir.getAbsolutePath();	
		String configPath = currentPath + "/config.txt";
		readConfigure(configPath);
		getPeerList(configPath);
		
		String conConfig = currentPath + "/" + self.peerName
				+ "/ConConfig.txt";
		readConsistencyConfig(conConfig);
		
		// for mater copies
		String peerSharedPath = currentPath + "/" + self.peerName
				+ "/" + "Shared";	
		getSharedList(peerSharedPath);
		shareFileListener(peerSharedPath);
		
		// for downloaded copies
		String peerDownloadPath = currentPath + "/" + self.peerName
				+ "/" + "Downloads";	
		downloadFileListener(peerDownloadPath);
		
		new Thread(new SetupListener()).start();
		new Thread(new SendListener()).start();
		Client.socket = new Socket[Client.neighborsCount];
	}

	public boolean hasConnected() {
		for (int i = 0; i < Client.neighborsCount; i++) {
			if (Client.neighbors[i] == null)
				return false;
		}
		return true;
	}

	// connect with a peer's neighbors.
	public void connect() {
		try {
			for (int i = 0; i < Client.neighborsCount; i++) {
				if (Client.socket[i] == null) {
					Client.socket[i] = new Socket(Client.neighbors[i].peerIP,
							Integer.parseInt(Client.neighbors[i].peerPort));
					dos = new DataOutputStream(
							Client.socket[i].getOutputStream());
					dos.writeUTF(Client.self.peerName);
				} else {
					// System.out.println(" Notice : "
					// + Client.neighbors[i].peerName + "has been occupied.");
				}
			}

			// start new threads to maintain connections.
			for (int i = 0; i < Client.neighborsCount; i++) {
				new Thread(new IndexHandler(i)).start();
			}
			System.out.println("Connected to Neighbors.");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// this method uses org.apache.commons.io.monitor to monitor the change of a given directory.
	// it is event-driven, file created, delete, change can trigger an event
	public void shareFileListener(final String filePath) 
	{
		FileAlterationObserver observer = null;
		try {
			// use log to print out each event
			final Log log = LogFactory.getLog(PeerInfo.class);
	        observer = new FileAlterationObserver(filePath, null, null);
	        observer.addListener(new FileAlterationListenerAdaptor(){
	        
	        @Override
	        public void onFileChange(File file) {
	            super.onFileChange(file);
	            log.info("File Changed: " +file.getAbsolutePath());
	            sharedFiles.get(file.getName()).versionIncrease();
	    		System.out.print("Shared Files: ");
	    		System.out.println(sharedFiles.keySet());
	        }

	        @Override
	        public void onFileCreate(File file) {
	            super.onFileCreate(file);
	            log.info("File Created: "+file.getAbsolutePath());
	            sharedFiles.put(file.getName(), new FileInfo(
	            		file.getName(), Client.self, Client.TTR));
	    		System.out.print("Shared Files: ");
	    		System.out.println(sharedFiles.keySet());
	        }

	        @Override
	        public void onFileDelete(File file) {
	            super.onFileDelete(file);
	            log.info("File Deleted: " +file.getAbsolutePath());
	            sharedFiles.remove(file.getName());
	    		System.out.print("Shared Files: ");
	    		System.out.println(sharedFiles.keySet());
	        }
	        });	        
	        long interval = 1000;
			FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
	        monitor.start();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}		
	
	// this method uses org.apache.commons.io.monitor to monitor the change of a given directory.
	// it is event-driven, file created, delete, change can trigger an event
	public void downloadFileListener(final String filePath) 
	{
		FileAlterationObserver observer = null;
		try {
			// use log to print out each event
	        observer = new FileAlterationObserver(filePath, null, null);
	        observer.addListener(new FileAlterationListenerAdaptor(){
	        
	        @Override
	        public void onFileChange(File file) {
	            super.onFileChange(file);
	            // log.info("File Changed: " +file.getAbsolutePath());
	            System.out.println("You are not ALLOWED to change downloaded file!!!");
	        }

	        @Override
	        public void onFileCreate(File file) {
	            super.onFileCreate(file);
	            // log.info("File Created: "+file.getAbsolutePath());
	    		System.out.print("Downloaded Files: ");
	    		System.out.println(downloadFiles.keySet());
	        }

	        @Override
	        public void onFileDelete(File file) {
	            super.onFileDelete(file);
	            // log.info("File Deleted: " +file.getAbsolutePath());
	            downloadFiles.remove(file.getName());
	    		System.out.print("Downloaded Files: ");
	    		System.out.println(downloadFiles.keySet());
	        }
	        });	        
	        long interval = 1000;
			FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
	        monitor.start();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}	
	
	public static void disconnect(Socket[] socket) {
		for (int i = 0; i < neighborsCount; i++) {
			try {
				socket[i].close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// check if the client already has this message
	public static boolean checkMessageArray(Message m) {
		for (int i = 0; i < Client.messageNumber; i++) {
			if (Client.messageArray[i].isEqual(m))
				return true;
		}
		return false;
	}

	@SuppressWarnings("resource")
	public void obtain(String fn) throws IllegalArgumentException, IOException, Exception {
		System.out
				.println("Please Input the peer name you "
						+ "want to download from :");
		Scanner input = new Scanner(System.in);
		String pn = input.nextLine();
		gson = new Gson();
		FileInfo finfo = new FileInfo();
		
		// check peer list to get destination's information
		for (int j = 0; j < 10; j++) {
			if (Client.peerList[j].peerName.equals(pn)){
				// Download
				Socket p2pSocket = new Socket(Client.peerList[j].peerIP, 
						Integer.parseInt(Client.peerList[j].peerPort) + 1);
				DataInputStream p2pIn = new DataInputStream(
						p2pSocket.getInputStream());
				DataOutputStream p2pOut = new DataOutputStream(
						p2pSocket.getOutputStream());
				p2pOut.writeUTF(fn);
				p2pOut.flush();
				
				String infoBuffer = p2pIn.readUTF();
				finfo = gson.fromJson(infoBuffer, finfo.getClass());
				downloadFiles.put(fn, finfo);
				
				// the Download process may not work when file size is larger 
				// than the buffersize
				int bufferSize = 10240;
				byte[] buf1 = new byte[bufferSize];
				int passedlen = 0;
				long len = 0;
				
				String receiveFileName = p2pIn.readUTF();
				String savePath = Client.self.peerPath + "/" + "Downloads" 
						+ "/" + receiveFileName;
				
				DataOutputStream fileOut = new DataOutputStream(
						new BufferedOutputStream(new BufferedOutputStream(
								new FileOutputStream(savePath))));
				
				// len: sent file's length
				len = p2pIn.readLong();
				System.out.println("File length: " + len / 1000 + " KB");
				System.out.println("Start Downloading " + receiveFileName + "...");
				
				while (true) {
					int read = 0;
					if (p2pIn != null) {
						read = p2pIn.read(buf1);
					}					
					passedlen += read;
					if (read == -1) {
						break;
					}
					
					// a simple indicator
					// may not work correctly when file is large
					System.out.println("File Received: "
				   		+ (passedlen * 100 / len) + "%");
					fileOut.write(buf1, 0, read);
					
				}
				System.out.println("Downlaod Complete!");
				//System.out.println("Save as: " + savePath);
				fileOut.close();
				break;
			}else {
				System.out.println("Invalid Peer Name!!!");
			}
		}
	}

	public void query(MessageID mID, int TTL, String searchFileName) {
		synchronized (lock){
		Message m = new Message(mID, TTL, searchFileName);
		try {
			for (int i = 0; i < Client.neighborsCount; i++) {
				DataOutputStream dos = new DataOutputStream(
						Client.socket[i].getOutputStream());
				dos.writeUTF("2");
				dos.flush();
				// send message
				Gson gson = new Gson();
				String sendBuffer = gson.toJson(m);
				dos.writeUTF(sendBuffer);
				dos.flush();
				// send upstream information
				sendBuffer = gson.toJson(Client.self);
				dos.writeUTF(sendBuffer);
				dos.flush();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}}

	@SuppressWarnings({ "resource", "static-access" })
	public static void main(String[] args) throws RuntimeException, Exception {
		Client client = new Client();

		Scanner input = null;
		int commandIndex;

		do {
			// simple user interface
			System.out.println("Please input an Index Number: ");
			System.out.println("1: Connect \n2: Query \n"
					+ "3: Download \n4: Query Test\n5: Show Files\n"
					+ "6: Quit");
			input = new Scanner(System.in);
			commandIndex = input.nextInt();
			switch (commandIndex) {
			case 1:
				client.connect();
				break;
				
			case 2:
				// do query
				Scanner inputS = new Scanner(System.in);
				System.out
						.println("Please input the exact file "
								+ "name you are looking for: ");
				String searchFileName = inputS.nextLine();

				System.out.println("Please input the TTL: ");
				int ttl = inputS.nextInt();
				
				Client.hitCount = 0;
				MessageID mID = new MessageID(client.self);
				client.query(mID, ttl, searchFileName);
				
				// when messageArray get to max
				// start from 0, as a flush action
				if (Client.messageNumber == 499){
					Client.messageNumber = 0;
				}
					
				break;
				
			case 3:
				Scanner obtainFile = new Scanner(System.in);
				System.out
						.println("Please input the exact file name "
								+ "you want to download :");
				String fn = obtainFile.nextLine();

				client.obtain(fn);

				break;
				
			case 4:
				// do query for 200 times
				Scanner test = new Scanner(System.in);
				System.out
						.println("Please input the exact file "
								+ "name you are looking for: ");
				String testFileName = test.nextLine();

				System.out.println("Please input the TTL: ");
				int testTTL = test.nextInt();
				
				synchronized (lock1){
				for (int i = 0; i < 200; i++){
					Client.startTime = System.currentTimeMillis();
					Client.hitCount = 0;
					
					MessageID testmID = new MessageID(client.self);
					client.query(testmID, testTTL, testFileName);
					
					// pause the loop for a short time
					// to make sure the timer is working correctly
					// a mutex lock is a plus
					// the sleep time acts actually as the cutoff time
					// a too long response time will not be record due to an EXCEPTION
					Thread thread = Thread.currentThread();
					thread.sleep(1000);
					}
				}
				
			case 5:
	    		System.out.print("Shared Files: ");
	    		System.out.println(sharedFiles.keySet());
	    		System.out.print("Downloaded Files: ");
	    		System.out.println(downloadFiles.keySet());
	    		
			}
		} while (commandIndex != 6);
		disconnect(socket);
	}
}
